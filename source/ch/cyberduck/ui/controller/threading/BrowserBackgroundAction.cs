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

using Ch.Cyberduck.Core;
using ch.cyberduck.core;
using ch.cyberduck.core.threading;
using java.util;

namespace Ch.Cyberduck.Ui.Controller.Threading
{
    public abstract class BrowserBackgroundAction : AlertRepeatableBackgroundAction
    {
        protected BrowserBackgroundAction(BrowserController controller) : base(controller, controller, controller)
        {
            BrowserController = controller;
        }

        public BrowserController BrowserController { get; private set; }

        public override List getSessions()
        {
            return Collections.singletonList(BrowserController.getSession());
        }

        public override void log(bool request, string message)
        {
            if (Preferences.instance().getBoolean("browser.transcript.open"))
            {
                AsyncController.AsyncDelegate mainAction = delegate { BrowserController.log(request, message); };
                BrowserController.Invoke(mainAction);
            }
        }

        public override void prepare()
        {
            AsyncController.AsyncDelegate mainAction = delegate
                {
                    BrowserController.View.StartActivityAnimation();
                    BrowserController.SetStatus(getActivity());
                };
            BrowserController.Invoke(mainAction);
            base.prepare();
        }

        public override void cancel()
        {
            if (isRunning())
            {
                List sessions = getSessions();
                foreach (var s in Utils.ConvertFromJavaList<Session>(sessions))
                {
                    try
                    {
                        s.interrupt();
                    }
                    catch (BackgroundException e)
                    {
                        error(e);
                    }
                }
            }
            base.cancel();
        }

        public override void finish()
        {
            base.finish();
            AsyncController.AsyncDelegate mainAction = delegate
                {
                    BrowserController.View.StopActivityAnimation();
                    BrowserController.SetStatus();
                };
            BrowserController.Invoke(mainAction);
        }
    }
}