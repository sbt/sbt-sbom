// SPDX-FileCopyrightText: 2023, Scala center, 2011 - 2022, Lightbend, Inc., 2008 - 2010, Mark Harrah
//
// SPDX-License-Identifier: Apache-2.0

package com.github.sbt.sbom

import sbt.librarymanagement.{ ConfigurationReport, ModuleID, ModuleReport }
import sbt.{ File, OrganizationArtifactReport, Logger }

import scala.collection.mutable

/*
 * taken from sbt at https://github.com/sbt/sbt/blob/1.10.x/main/src/main/scala/sbt/internal/graph/backend/SbtUpdateReport.scala
 *
 * Copyright 2023, Scala center
 * Copyright 2011 - 2022, Lightbend, Inc.
 * Copyright 2008 - 2010, Mark Harrah
 * Licensed under Apache License 2.0 (see LICENSE)
 */
object SbtUpdateReport {
  case class Module(
      id: GraphModuleId,
      license: Option[String] = None,
      extraInfo: String = "",
      evictedByVersion: Option[String] = None,
      jarFile: Option[File] = None,
      error: Option[String] = None,
      qualifier: Map[String, String] = Map[String, String]()
  )

  private type Edge = (GraphModuleId, GraphModuleId)
  private def Edge(from: GraphModuleId, to: GraphModuleId): Edge = from -> to

  case class ModuleGraph(nodes: Seq[Module], edges: Seq[Edge]) {
    lazy val modules: Map[GraphModuleId, Module] =
      nodes.map(n => (n.id, n)).toMap

    def module(id: GraphModuleId): Option[Module] = modules.get(id)

    lazy val dependencyMap: Map[GraphModuleId, Seq[Module]] =
      createMap(identity)

    def createMap(
        bindingFor: ((GraphModuleId, GraphModuleId)) => (GraphModuleId, GraphModuleId)
    ): Map[GraphModuleId, Seq[Module]] = {
      val m = new mutable.HashMap[GraphModuleId, mutable.Set[Module]] with mutable.MultiMap[GraphModuleId, Module]
      edges.foreach { entry =>
        val (f, t) = bindingFor(entry)
        module(t).foreach(m.addBinding(f, _))
      }
      m.toMap.mapValues(_.toSeq.sortBy(_.id.idString)).toMap.withDefaultValue(Nil)
    }

    def roots: Seq[Module] =
      nodes.filter(n => !edges.exists(_._2 == n.id)).sortBy(_.id.idString)
  }

  case class GraphModuleId(organization: String, name: String, version: String) {
    def idString: String = organization + ":" + name + ":" + version
  }
  object GraphModuleId {
    def apply(sbtId: ModuleID): GraphModuleId =
      GraphModuleId(sbtId.organization, sbtId.name, sbtId.revision)
  }


  def getModuleQualifier(moduleReport: ModuleReport, log: Option[Logger] = None): Map[String, String] = {
    val qualifier = new mutable.HashMap[String, String]()

    // Getting artifact with the same name as module name as purl qualifier
    val moduleArtifacts = moduleReport.artifacts.filter(ar =>{
      ar._1.name.equals(moduleReport.module.name)
    })

    moduleArtifacts.size match {
      case 0 => () // ignore empty found artifacts
      case x =>
        if (x > 1 && log.isDefined) {
          log.foreach(_.warn("Multiple artifacts with the same name as module name are detected. Taking the first artifact match as Purl qualifier."))
        }
        if (moduleArtifacts.head._1.`type`.nonEmpty) qualifier.put("type", moduleArtifacts.head._1.`type`)
        moduleArtifacts.head._1.classifier.foreach(classifier => if (classifier.nonEmpty) {
          qualifier.put("classifier", classifier)
        })
    }

    qualifier.toMap
  }

  def fromConfigurationReport(report: ConfigurationReport, rootInfo: ModuleID, log: Logger): ModuleGraph = {
    def moduleEdges(orgArt: OrganizationArtifactReport): Seq[(Module, Seq[Edge])] = {
      val chosenVersion = orgArt.modules.find(!_.evicted).map(_.module.revision)
      orgArt.modules.map(moduleEdge(chosenVersion))
    }

    def moduleEdge(chosenVersion: Option[String])(report: ModuleReport): (Module, Seq[Edge]) = {
      val evictedByVersion = if (report.evicted) chosenVersion else None
      val jarFile = report.artifacts
        .find(_._1.`type` == "jar")
        .orElse(report.artifacts.find(_._1.extension == "jar"))
        .map(_._2)
      (
        Module(
          id = GraphModuleId(report.module),
          license = report.licenses.headOption.map(_._1),
          evictedByVersion = evictedByVersion,
          jarFile = jarFile,
          error = report.problem,
          qualifier = getModuleQualifier(report)
        ),
        report.callers.map(caller => Edge(GraphModuleId(caller.caller), GraphModuleId(report.module)))
      )
    }

    val (nodes, edges) = report.details.flatMap(moduleEdges).unzip
    val root = Module(GraphModuleId(rootInfo))

    ModuleGraph(root +: nodes, edges.flatten)
  }
}
