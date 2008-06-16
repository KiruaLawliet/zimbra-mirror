/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.qa.unittest;

import java.util.concurrent.Callable;

import com.zimbra.cs.mailbox.ScheduledTask;

/**
 * Task used by {@link TestScheduledTaskManager}.
 * 
 * @author bburtin
 *
 */
public class TestTask
extends ScheduledTask
implements Callable<Void> {
    
    int mNumCalls = 0;
    
    public TestTask() {
    }
    
    public String getName() {
        return TestScheduledTaskManager.TASK_NAME;
    }
    
    public int getNumCalls() {
        return mNumCalls;
    }
    
    public Void call() {
        mNumCalls++;
        return null;
    }
}