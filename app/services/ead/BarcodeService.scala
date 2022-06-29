/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services.ead

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64

import javax.imageio.ImageIO
import javax.inject.Inject
import org.krysalis.barcode4j.HumanReadablePlacement
import org.krysalis.barcode4j.impl.code128.Code128Bean
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider
import org.krysalis.barcode4j.tools.UnitConv

class BarcodeService @Inject() (code128Bean: Code128Bean) {
  private val dpi = 200

  def base64Image(mrn: String) = {
    code128Bean.setModuleWidth(UnitConv.in2mm(1.0f / dpi))
    code128Bean.setMsgPosition(HumanReadablePlacement.HRP_NONE)
    code128Bean.doQuietZone(false)

    val canvas = new BitmapCanvasProvider(dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0)
    code128Bean.generateBarcode(canvas, mrn)

    val outputStream = new ByteArrayOutputStream
    ImageIO.write(canvas.getBufferedImage, "png", outputStream)
    outputStream.flush()
    canvas.finish()
    outputStream.close()

    Base64.getEncoder.encodeToString(outputStream.toByteArray)
  }
}
