package io.github.siculo.sbtbom

import sbt.*

class MakeBomTask(properties: BomTaskProperties,
                  bomFile: File)
  extends BomTask[File](properties) {

  override def execute: File = {
    log.info(s"Creating bom file ${bomFile.getAbsolutePath}")
    val bomText = getBomText
    writeToFile(bomFile, bomText)
    validateBomFile(bomFile)
    log.info(s"Bom file ${bomFile.getAbsolutePath} created")
    bomFile
  }
}

