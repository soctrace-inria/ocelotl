/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilenameValidator {
	
	private static final Logger logger = LoggerFactory.getLogger(FilenameValidator.class);
	
	/**
	 * Check that a name is valid given an OS names
	 * 
	 * @param text
	 * @return
	 */
	public static String checkNameValidity(String text) {

		String osName = System.getProperty("os.name").toLowerCase();
		String fileName = text;

		// Is it a windows OS
		if (osName.contains("win"))
			return isValidWindowsName(text);

		return fileName;
	}

	/**
	 * Check that the provided filename satisfies the Windows constraints
	 * 
	 * @param text
	 *            the tested file name
	 * @return the same string as input if is valid, a string with the current
	 *         date and time otherwise
	 */
	public static String isValidWindowsName(String text) {
		String result = text;
		Pattern pattern = Pattern
				.compile(
						"# Match a valid Windows filename (unspecified file system).          \n"
								+ "^                                # Anchor to start of string.        \n"
								+ "(?!                              # Assert filename is not: CON, PRN, \n"
								+ "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n"
								+ "    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n"
								+ "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n"
								+ "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n"
								+ "  (?:\\.[^.]*)?                  # followed by optional extension    \n"
								+ "  $                              # and end of string                 \n"
								+ ")                                # End negative lookahead assertion. \n"
								+ "[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n"
								+ "[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n"
								+ "$                                # Anchor to end of string.            ",
						Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
								| Pattern.COMMENTS);
		Matcher matcher = pattern.matcher(text);
		boolean isMatch = matcher.matches();

		// Was the name invalid
		if (!isMatch) {
			Date theDate = new Date(System.currentTimeMillis());
			result = new SimpleDateFormat("dd-MM-yyyy HHmmss z")
					.format(theDate);
			logger.error("Some incompatible characters with the operating system (windows) were found in the file name and will be replace by: "
					+ result);
		}

		return result;
	}
}
