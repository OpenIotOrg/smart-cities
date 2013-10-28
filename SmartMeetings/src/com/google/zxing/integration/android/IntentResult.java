/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.integration.android;


/**
 * <p>
 * Encapsulates the result of a barcode scan invoked through
 * {@link IntentIntegrator}.
 * </p>
 * 
 * @author Sean Owen
 */
public final class IntentResult {

	private final String contents;
	private final String formatName;
	private final byte[] rawBytes;
	private final Integer orientation;
	private final String errorCorrectionLevel;

	IntentResult() {
		this(null, null, null, null, null);
	}

	IntentResult(final String contents, final String formatName,
			final byte[] rawBytes, final Integer orientation,
			final String errorCorrectionLevel) {
		this.contents = contents;
		this.formatName = formatName;
		this.rawBytes = rawBytes;
		this.orientation = orientation;
		this.errorCorrectionLevel = errorCorrectionLevel;
	}

	/**
	 * @return raw content of barcode
	 */
	public String getContents() {
		return this.contents;
	}

	/**
	 * @return name of format, like "QR_CODE", "UPC_A". See
	 *         {@code BarcodeFormat} for more format names.
	 */
	public String getFormatName() {
		return this.formatName;
	}

	/**
	 * @return raw bytes of the barcode content, if applicable, or null
	 *         otherwise
	 */
	public byte[] getRawBytes() {
		return this.rawBytes;
	}

	/**
	 * @return rotation of the image, in degrees, which resulted in a successful
	 *         scan. May be null.
	 */
	public Integer getOrientation() {
		return this.orientation;
	}

	/**
	 * @return name of the error correction level used in the barcode, if
	 *         applicable
	 */
	public String getErrorCorrectionLevel() {
		return this.errorCorrectionLevel;
	}

	@Override
	public String toString() {
		StringBuilder dialogText = new StringBuilder(100);
		dialogText.append("Format: ").append(this.formatName).append('\n');
		dialogText.append("Contents: ").append(this.contents).append('\n');
		int rawBytesLength = this.rawBytes == null ? 0 : this.rawBytes.length;
		dialogText.append("Raw bytes: (").append(rawBytesLength)
				.append(" bytes)\n");
		dialogText.append("Orientation: ").append(this.orientation)
				.append('\n');
		dialogText.append("EC level: ").append(this.errorCorrectionLevel)
				.append('\n');
		return dialogText.toString();
	}

}
