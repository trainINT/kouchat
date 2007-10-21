
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

package net.usikkert.kouchat.misc;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Can load a wav file, and play it.
 * 
 * @author Christian Ihle
 */
public class SoundBeeper
{
	private Clip clip;

	/**
	 * Plays the loaded wav file.
	 */
	public synchronized void beep()
	{
		if ( clip != null && !clip.isActive() )
		{
			clip.setFramePosition( 0 );
			clip.start();
		}
	}

	/**
	 * Loads a wav file.
	 * 
	 * @param fileName The wav file to load.
	 */
	public void loadWavClip( String fileName )
	{
		if ( fileName.endsWith( ".wav" ) )
		{
			try
			{
				AudioInputStream stream = AudioSystem .getAudioInputStream( new File( fileName ) );
				AudioFormat format = stream.getFormat();
				DataLine.Info info = new DataLine.Info( Clip.class, format );

				if ( AudioSystem.isLineSupported( info ) )
				{
					clip = (Clip) AudioSystem.getLine( info );
					clip.open( stream );
					stream.close();
				}
			}

			catch ( UnsupportedAudioFileException e )
			{
				e.printStackTrace();
			}

			catch ( IOException e )
			{
				e.printStackTrace();
			}

			catch ( LineUnavailableException e )
			{
				e.printStackTrace();
			}
		}
	}
}