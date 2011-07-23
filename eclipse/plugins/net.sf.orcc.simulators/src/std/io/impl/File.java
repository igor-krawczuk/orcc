/*
 * Copyright (c) 2011, EPFL
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the EPFL nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package std.io.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.sf.orcc.simulators.SimulatorDescriptor;

/**
 * This class defines native functions for the File unit.
 * 
 * This class uses the SimulatorDecriptor class to handle descriptors.
 * 
 * @author Thavot Richard
 * 
 */
public class File {

	public static Integer accessFile(String path) {
		try {
			return SimulatorDescriptor.openFile(path);
		} catch (FileNotFoundException e) {
			String msg = "I/O error : A file cannot be open";
			throw new RuntimeException(msg, e);
		}
	}

	public static Integer closeFile(Integer desc) {
		if (SimulatorDescriptor.containsFile(desc)) {
			try {
				SimulatorDescriptor.closeFile(desc);
			} catch (IOException e) {
				String msg = "I/O error : A file cannot be close";
				throw new RuntimeException(msg, e);
			}
		}
		return 0;
	}

	public static Integer readByte(Integer desc) {
		if (SimulatorDescriptor.containsFile(desc)) {
			try {
				return new Integer(SimulatorDescriptor.getFile(desc).readByte());
			} catch (IOException e) {
				String msg = "I/O error : readByte function";
				throw new RuntimeException(msg, e);
			}
		}
		return 0;
	}

	public static Integer readUnsignedByte(Integer desc) {
		if (SimulatorDescriptor.containsFile(desc)) {
			try {
				return SimulatorDescriptor.getFile(desc).readUnsignedByte();
			} catch (IOException e) {
				String msg = "I/O error : readUnsignedByte function";
				throw new RuntimeException(msg, e);
			}
		}
		return 0;
	}

	public static Integer sizeOfFile(Integer desc) {
		if (SimulatorDescriptor.containsFile(desc)) {
			try {
				return (int) SimulatorDescriptor.getFile(desc).length();
			} catch (IOException e) {
				String msg = "I/O error : sizeOfFile function";
				throw new RuntimeException(msg, e);
			}
		}
		return 0;
	}

	public static void writeUnsignedByte(Integer desc, Integer v) {
		if (SimulatorDescriptor.containsFile(desc)) {
			try {
				SimulatorDescriptor.getFile(desc).write(v);
			} catch (IOException e) {
				String msg = "I/O error : writeUnsignedByte function";
				throw new RuntimeException(msg, e);
			}
		}
	}

	public static void writeByte(Integer desc, Integer v) {
		if (SimulatorDescriptor.containsFile(desc)) {
			try {
				SimulatorDescriptor.getFile(desc).write(v);
			} catch (IOException e) {
				String msg = "I/O error : writeByte function";
				throw new RuntimeException(msg, e);
			}
		}
	}

	public static void seek(Integer desc, Integer pos) {
		if (SimulatorDescriptor.containsFile(desc)) {
			try {
				SimulatorDescriptor.getFile(desc).seek(pos);
			} catch (IOException e) {
				String msg = "I/O error : seek function";
				throw new RuntimeException(msg, e);
			}
		}
	}

	public static Integer filePointer(Integer desc) {
		if (SimulatorDescriptor.containsFile(desc)) {
			try {
				return (int) SimulatorDescriptor.getFile(desc).getFilePointer();
			} catch (IOException e) {
				String msg = "I/O error : filePointer function";
				throw new RuntimeException(msg, e);
			}
		}
		return 0;
	}

}