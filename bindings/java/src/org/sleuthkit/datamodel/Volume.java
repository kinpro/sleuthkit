/*
 * Autopsy Forensic Browser
 * 
 * Copyright 2011 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
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
package org.sleuthkit.datamodel;

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a volume in a VolumeSystem, object stored in tsk_vs_parts table.
 * Populated based on data in database.
 */
public class Volume extends AbstractContent {
	
	private long addr;
	private long start; //in sectors, relative to volume system start
	private long length; //in sectors
	private long flags;
	private String desc;
	private volatile long volumeHandle = 0;

	/**
	 * Constructor to create the data object mapped from tsk_vs_parts entry
	 *
	 * @param db database object
	 * @param obj_id
	 * @param addr
	 * @param start	in sectors, relative to start of VS
	 * @param length in sectors
	 * @param flags
	 * @param desc
	 */
	protected Volume(SleuthkitCase db, long obj_id, long addr, long start, long length, long flags, String desc) {
		super(db, obj_id, "vol" + Long.toString(addr)); //NON-NLS
		this.addr = addr;
		this.start = start;
		this.length = length;
		this.flags = flags;
		if (!desc.equals("")) {
			this.desc = desc;
		} else {
			this.desc = NbBundle.getMessage(Volume.class, "Volume.desc.text");
		}
	}

	@Override
	public int read(byte[] buf, long offset, long len) throws TskCoreException {
		synchronized (this) {
			Content myParent = getParent();
			if (!(myParent instanceof VolumeSystem)) {
				throw new TskCoreException(NbBundle.getMessage(Volume.class, "Volume.read.exception.msg1.text"));
			}
			VolumeSystem parentVs = (VolumeSystem) myParent;
			// read from the volume
			if (volumeHandle == 0) {
				volumeHandle = SleuthkitJNI.openVsPart(parentVs.getVolumeSystemHandle(), addr);
			}

		}
		return SleuthkitJNI.readVsPart(volumeHandle, buf, offset, len);
	}

	@Override
	public void close() {
		if (volumeHandle != 0) {
			synchronized(this) {
				if (volumeHandle != 0) {
					SleuthkitJNI.closeVs(volumeHandle);
					volumeHandle = 0;
				}
			}
		}
	}
	
	@Override
	public void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}
	
	

	@Override
	public long getSize() {
		// size of the volume
		return length;
	}

	@Override
	public synchronized String getUniquePath() throws TskCoreException {
		String uniquePath = "";
		String name = getName();
		if (!name.isEmpty()) {
			uniquePath = "/vol_" + name; //NON-NLS
		}

		Content myParent = getParent();
		if (myParent != null) {
			uniquePath = myParent.getUniquePath() + uniquePath;
		}
		return uniquePath;
	}

	//methods get exact data from database. could be manipulated to get more
	//meaningful data.
	/**
	 * get the unique partition address within this volume system (assigned by The Sleuth Kit)
	 *
	 * @return partition address in volume system
	 */
	public long getAddr() {
		return addr;
	}

	/**
	 * get the starting sector address of this volume relative to start of the volume system
	 *
	 * @return starting address
	 */
	public long getStart() {
		return start;
	}

	/**
	 * get the length of the volume in sectors
	 *
	 * @return length
	 */
	public long getLength() {
		return length;
	}

	/**
	 * get the flags
	 *
	 * @return flags
	 */
	public long getFlags() {
		return flags;
	}

	/**
	 * get the flags as String
	 *
	 * @return flags as String
	 */
	public String getFlagsAsString() {
		return Volume.vsFlagToString(flags);
	}

	/**
	 * get the description. This is set by the volume system and doesn't exist for all volumes.
	 *
	 * @return description
	 */
	public String getDescription() {
		return desc;
	}

	// ----- Here all the methods for vs flags conversion / mapping -----
	/**
	 * Convert volume type flag to string
	 *
	 * @param vsFlag long flag to convert
	 * @return string representation
	 */
	public static String vsFlagToValue(long vsFlag) {

		String result = "";

		for (TskData.TSK_VS_PART_FLAG_ENUM flag : TskData.TSK_VS_PART_FLAG_ENUM.values()) {
			if (flag.getVsFlag() == vsFlag) {
				result = flag.toString();
			}
		}
		return result;
	}

	/**
	 * Convert volume flag string to long
	 *
	 * @param vsFlag string representation of the flag
	 * @return long representation of the flag
	 */
	public static long valueToVsFlag(String vsFlag) {

		long result = 0;

		for (TskData.TSK_VS_PART_FLAG_ENUM flag : TskData.TSK_VS_PART_FLAG_ENUM.values()) {
			if (flag.toString().equals(vsFlag)) {
				result = flag.getVsFlag();
			}
		}
		return result;
	}

	/**
	 * Convert long representation of the flag to user readable format
	 *
	 * @param vsFlag long repr. of the flag
	 * @return user readable string representation
	 */
	public static String vsFlagToString(long vsFlag) {

		String result = "";

		long allocFlag = TskData.TSK_VS_PART_FLAG_ENUM.TSK_VS_PART_FLAG_ALLOC.getVsFlag();
		long unallocFlag = TskData.TSK_VS_PART_FLAG_ENUM.TSK_VS_PART_FLAG_UNALLOC.getVsFlag();

		// some variables that might be needed in the future
		long metaFlag = TskData.TSK_VS_PART_FLAG_ENUM.TSK_VS_PART_FLAG_META.getVsFlag();
		long allFlag = TskData.TSK_VS_PART_FLAG_ENUM.TSK_VS_PART_FLAG_ALL.getVsFlag();

		if ((vsFlag & allocFlag) == allocFlag) {
			result = NbBundle.getMessage(Volume.class, "Volume.vsFlagToString.allocated");
		}
		if ((vsFlag & unallocFlag) == unallocFlag) {
			result = NbBundle.getMessage(Volume.class, "Volume.vsFlagToString.unallocated");
		}
		// ... add more code here if needed

		return result;
	}

	@Override
	public <T> T accept(SleuthkitItemVisitor<T> v) {
		return v.visit(this);
	}

	@Override
	public <T> T accept(ContentVisitor<T> v) {
		return v.visit(this);
	}

	@Override
	public List<Content> getChildren() throws TskCoreException {
		return getSleuthkitCase().getVolumeChildren(this);
	}

	@Override
	public List<Long> getChildrenIds() throws TskCoreException {
		return getSleuthkitCase().getVolumeChildrenIds(this);
	}

	@Override
	public Image getImage() throws TskCoreException {
		return getParent().getImage();
	}

	/**
	 * @return a list of FileSystem that are direct descendents of this Image.
	 * @throws TskCoreException
	 */
	public List<FileSystem> getFileSystems() throws TskCoreException {

		List<Content> children = getChildren();
		List<FileSystem> fileSystems = new ArrayList<FileSystem>();
		for (Content child : children) {
			if (child instanceof FileSystem) {
				fileSystems.add((FileSystem) child);
			}
		}

		return fileSystems;
	}

	@Override
	public String toString(boolean preserveState) {
		return super.toString(preserveState) + "Volume [\t" + "addr " + addr + "\t" + "desc " + desc + "\t" + "flags " + flags + "\t" + "length " + length + "\t" + "start " + start + "]\t"; //NON-NLS
	}
}
