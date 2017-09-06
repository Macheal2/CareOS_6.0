/**
 *  
 * Copyright (C) 2016 The Cappu Android Source Project
 * <p>
 * Licensed under the Cappu License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.cappu.cn
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2016年12月1日
 * @author: huangminqi@cappu.cn
 * @company: Cappu Co.,Ltd. 
 */
package com.cappu.pictorial;

/**
 * Created by hmq on 16-12-1.
 */

public enum DownloadState {
    WAITING(0), STARTED(1), FINISHED(2), STOPPED(3), ERROR(4);

    private final int value;

    DownloadState(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static DownloadState valueOf(int value) {
        switch (value) {
            case 0:
                return WAITING;
            case 1:
                return STARTED;
            case 2:
                return FINISHED;
            case 3:
                return STOPPED;
            case 4:
                return ERROR;
            default:
                return STOPPED;
        }
    }
}