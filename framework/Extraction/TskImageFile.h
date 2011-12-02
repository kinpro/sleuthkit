/*
 *
 *  The Sleuth Kit
 *
 *  Contact: Brian Carrier [carrier <at> sleuthkit [dot] org]
 *  Copyright (c) 2010-2011 Basis Technology Corporation. All Rights
 *  reserved.
 *
 *  This software is distributed under the Common Public License 1.0
 */

/**
 * \file TskImageFile.h
 * Contains the interface for the TskImageFile class.
 */

#ifndef _TSK_IMAGEFILE_H
#define _TSK_IMAGEFILE_H

#include "framework_i.h"
#include <vector>
#include <string>

/// An interface to a disk image.
/**
 * TskImageFile defines an interface for interacting with a disk image.
 * It supports opening split image files, extracting file system 
 * information from the image and extracting data for a specific file
 * or for a range of sectors.
 */
class TSK_FRAMEWORK_API TskImageFile
{
public:
    TskImageFile();

    virtual ~TskImageFile();

    /// Open the disk image represented by one or more actual files.
    /**
     * @param imageFiles One or more files that make up the disk image
     * @return 0 on success and -1 on error
     */
    virtual int open(const std::vector<std::wstring> &) = 0;

    // open the images in the DB
    virtual int open() = 0;

    /// Close the disk image.
    virtual void close() = 0;

    /// Return the file name(s) that make up the image.
    virtual std::vector<std::wstring> filenames() const = 0;

    /// Extract file system files from the disk image.
    virtual int extractFiles() = 0;

    /// Return the data located at the given offset in the disk image.
    /**
     * @param sect_start Sector offset into image from which to return data
     * @param sect_len Number of sectors to read
     * @param buffer A buffer into which data will be placed. Must be at
     * least len * 512 large
     * @return Number of sectors read or -1 on error
     */
    virtual int getSectorData(const uint64_t sect_start, 
                              const uint64_t sect_len, 
                              char *buffer) = 0;

    /// Return the data located at the given byte offset in the disk image.
    /**
     * @param byte_start Byte offset into image from which to return data
     * @param byte_len Number of bytes to read
     * @param buffer A buffer into which data will be placed. Must be at
     * least byte_len large
     * @return Number of bytes read or -1 on error
     */
    virtual int getByteData(const uint64_t byte_start, 
                            const uint64_t byte_len, 
                            char *buffer) = 0;

    /// Provides access to a file system file within the image.
    /**
     * @param fileId Framework identifier for a file
     * @return a handle that can be used to interact with the file or
     * -1 on error.
     */
    virtual int openFile(const uint64_t fileId) = 0;

    /// Read file content from the disk image
    /**
     * Reads content from the image file into the provided buffer for the given
     * file handle.
     * @param handle File handle that was returned by an earlier call to openFile()
     * @param byte_offset Starting byte offset from which to read data
     * @param byte_len The number of bytes to read
     * @param buffer A buffer into which data will be placed. Must be at least
     * byte_len bytes.
     * @return Number of bytes read or -1 on error
     */
    virtual int readFile(const int handle, 
                         const uint64_t byte_offset, 
                         const size_t byte_len, 
                         char * buffer) = 0;

    /// Close a previously opened file
    /**
     * @param handle File handle that was returned by an earlier call to openFile()
     */
    virtual int closeFile(const int handle) = 0;

private:

};

#endif