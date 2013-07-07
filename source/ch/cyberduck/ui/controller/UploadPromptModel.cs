﻿// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 

using ch.cyberduck.core;
using ch.cyberduck.core.transfer;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class UploadPromptModel : TransferPromptModel
    {
        private readonly Filter _filter = new UploadPathFilter();

        public UploadPromptModel(TransferPromptController controller, Transfer transfer) : base(controller, transfer)
        {
            ;
        }

        /// <summary>
        /// Filtering what files are displayed. Used to
        /// decide which files to include in the prompt dialog
        /// </summary>
        /// <returns></returns>
        public override Filter Filter()
        {
            return _filter;
        }

        public override object GetSize(Path path)
        {
            return path.attributes().getSize();
        }

        public override object GetWarningImage(Path path)
        {
            if (path.attributes().isFile())
            {
                if (path.attributes().getSize() == 0)
                {
                    return AlertIcon;
                }
                if (path.attributes().getSize() > path.getLocal().attributes().getSize())
                {
                    return AlertIcon;
                }
            }
            return null;
        }

        internal class UploadPathFilter : PromptFilter
        {
            protected static Logger Log = Logger.getLogger(typeof (UploadPathFilter).FullName);

            public override bool accept(object ap)
            {
                Path child = (Path) ap;
                Log.debug("accept:" + child);
                if (child.exists())
                {
                    if (child.attributes().getSize() == -1)
                    {
                        child.readSize();
                    }
                    if (child.getSession().isReadTimestampSupported())
                    {
                        if (child.attributes().getModificationDate() == -1)
                        {
                            child.readTimestamp();
                        }
                    }
                    return base.accept(child);
                }
                return false;
            }
        }
    }
}