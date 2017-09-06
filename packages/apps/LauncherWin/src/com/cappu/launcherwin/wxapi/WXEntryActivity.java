/**
 * 
 */
package com.cappu.launcherwin.wxapi;

import java.util.logging.LogManager;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/******************************************************************************
 * @function：
 * @author kook    QQ:329716228
 * @file_name WXEntryActivity.java
 * @package_name：com.cappu.launcherwin.wxapi
 * @project_name：LauncherWin
 * @department：卡布奇诺研发部
 * CAPPU Technology CO.,LTD 2016-1-4 All Rights Reserved.
 *
 *      http://www.cappu.com
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 * 修  改  人 :
 * 修改时间 :
 * 修改内容 :
 * ****************************************************************************
 */

/** 微信客户端回调activity示例 */  
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    
    private String TAG = "WXEntryActivity";
    // IWXAPI 是第三方app和微信通信的openapi接口  
    private IWXAPI api;  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        api = WXAPIFactory.createWXAPI(this, "wx312a5d621aceeace", false);//这里替换第一步申请的APP_ID wxda7d0f5775855c33
        api.handleIntent(getIntent(), this);  
        Log.i(TAG, "onCreate: jeff"+api.toString());
        super.onCreate(savedInstanceState);  
    }  
    @Override  
    public void onReq(BaseReq arg0) { }  
  
    @Override  
    public void onResp(BaseResp resp) {  
        Log.i(TAG, "BaseResp:"+resp.errCode);
        //LogManager.show(TAG, "resp.errCode:" + resp.errCode + ",resp.errStr:" + resp.errStr, 1);  
        switch (resp.errCode) {  
        case BaseResp.ErrCode.ERR_OK:  
            //分享成功
            finish();
            break;  
        case BaseResp.ErrCode.ERR_USER_CANCEL:
            finish();
            //分享取消  
            break;  
        case BaseResp.ErrCode.ERR_AUTH_DENIED:
            finish();
            //分享拒绝  
            break;  
        }  
    }  
}  

