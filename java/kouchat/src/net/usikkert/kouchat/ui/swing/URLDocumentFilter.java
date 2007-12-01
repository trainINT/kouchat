
/***************************************************************************
 *   Copyright 2006-2007 by Christian Ihle                                 *
 *   kontakt@usikkert.net                                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package net.usikkert.kouchat.ui.swing;

import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * This document filter is used to highlight urls added to a StyledDocument.
 * The current form of highlighting is underlining the url.
 * 
 * 3 different urls are recognized:<br />
 * protocol://host<br />
 * host.name<br />
 * \\host<br />
 * 
 * @author Christian Ihle
 */
public class URLDocumentFilter extends DocumentFilter
{
	/**
	 * The url is saved as an attribute in the Document, so
	 * this attribute can be used to retrieve the url later.
	 */
	public static final String URL_ATTRIBUTE = "url.attribute";

	private Pattern protPattern, dotPattern, backslashPattern;

	/**
	 * Constructor. Creates regex patterns to use for url checking.
	 */
	public URLDocumentFilter()
	{
		protPattern = Pattern.compile( "\\w{2,}://\\S+.+" );
		dotPattern = Pattern.compile( "\\w{1,}\\S*\\.\\p{Lower}{2,4}.+" );
		backslashPattern = Pattern.compile( "\\\\\\\\\\p{Alnum}.+" );
	}

	/**
	 * Inserts the text at the end of the Document, and checks if any parts
	 * of the text contains any urls. If a url is found, it is underlined
	 * and saved in an attribute.
	 */
	@Override
	public void insertString( final FilterBypass fb, final int offset, final String text, AttributeSet attr ) throws BadLocationException
	{
		super.insertString( fb, offset, text, attr );
		
		// Make a copy now, or else it could change if another message comes
		final MutableAttributeSet urlAttr = (MutableAttributeSet) attr.copyAttributes();

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				int startPos = findURLPos( text, 0 );

				if ( startPos != -1 )
				{
					StyleConstants.setUnderline( urlAttr, true );
					StyledDocument doc = (StyledDocument) fb.getDocument();

					while ( startPos != -1 )
					{
						int stopPos = -1;

						stopPos = text.indexOf( " ", startPos );

						if ( stopPos == -1 )
							stopPos = text.indexOf( "\n", startPos );

						urlAttr.addAttribute( URL_ATTRIBUTE, text.substring( startPos, stopPos ) );
						doc.setCharacterAttributes( offset + startPos, stopPos - startPos, urlAttr, false );
						startPos = findURLPos( text, stopPos );
					}
				}
			}
		} ).start();
	}

	/**
	 * Returns the position of the first matching
	 * url in the text, starting from the specified offset.
	 * 
	 * @param text The text to find urls in.
	 * @param offset Where in the text to begin the search.
	 * @return The position of the first character in the url, or -1
	 * if no url was found.
	 */
	private int findURLPos( String text, int offset )
	{
		int prot = text.indexOf( "://", offset );
		int dot = text.indexOf( ".", offset );
		int backslash = text.indexOf( " \\", offset );

		int firstMatch = -1;
		boolean retry = true;

		// Needs to loop because the text can get through the first test above,
		// but fail the regex match. If another url exists after the failed regex
		// match, it will not be found.
		while ( retry )
		{
			retry = false;

			if ( prot != -1 && ( prot < firstMatch || firstMatch == -1 ) )
			{
				int protStart = text.lastIndexOf( ' ', prot ) +1;
				String t = text.substring( protStart, text.length() -1 );

				if ( protPattern.matcher( t ).matches() )
					firstMatch = protStart;

				else
				{
					prot = text.indexOf( "://", prot +1 );

					if ( prot != -1 && ( prot < firstMatch || firstMatch == -1 ) )
						retry = true;
				}
			}
			
			if ( backslash != -1 && ( backslash < firstMatch || firstMatch == -1 ) )
			{
				String t = text.substring( backslash +1, text.length() -1 );

				if ( backslashPattern.matcher( t ).matches() )
					firstMatch = backslash +1;

				else
				{
					backslash = text.indexOf( " \\", backslash +1 );

					if ( backslash != -1 && ( backslash < firstMatch || firstMatch == -1 ) )
						retry = true;
				}
			}
			
			if ( dot != -1 && ( dot < firstMatch || firstMatch == -1 ) )
			{
				int dotStart = text.lastIndexOf( ' ', dot ) +1;
				String t = text.substring( dotStart, text.length() -1 );

				if ( dotPattern.matcher( t ).matches() )
					firstMatch = dotStart;

				else
				{
					dot = text.indexOf( ".", dot +1 );

					if ( dot != -1 && ( dot < firstMatch || firstMatch == -1 ) )
						retry = true;
				}
			}
		}

		return firstMatch;
	}
}
