package android.os.CaseType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.SparseArray;

import com.cappu.download.R;

public class ChannelManager {
    
    /***************  以下类型将在xml文件里面一一对应  如果有新的在xml里面添加了这里对应添加  *******************/
    /**通知栏推送通知*/
    public static final String PUSH_TYPE_NOTIFICATION= "notification";
    /**新闻类推送*/
    public static final String PUSH_TYPE_NEWS = "news";
    /**生活圈推送*/
    public static final String PUSH_TYPE_APK_LIFI = "living_area";
    /**游戏圈推送*/
    public static final String PUSH_TYPE_APK_GAME = "game_area";
    /** 普通apk 下载 */
    public static final String PUSH_TYPE_APK_DOWNLOAD = "download";
    
    /**************  以上类型将在xml文件里面一一对应  如果有新的在xml里面添加了这里对应添加   end *****************/
    
    private static String mRootCode = "cappu";
    
    static String TAG = "ChannelManager";
    public String mCurrentBrands;
    Context mContext;
    public ChannelManager(Context mContext) {
        this.mCurrentBrands = mContext.getResources().getString(R.string.preject_name);
    }
    
    public static class Query {
        private Context mContext;
        private List<ChannelType> mList;
        
        private String mCategory;
        public Query(Context context) {
            if (context == null) {
                throw new NullPointerException();
            }else{
                this.mContext = context;
                parseXML();
            }
        }
        
        public List<ChannelType> parseXML(){
            if(mList == null){
                mList = new ArrayList<ChannelType>();
            }else{
                mList.clear();
            }
            
            try {
                XmlResourceParser parser = null;
                parser = mContext.getResources().getXml(R.xml.channel);
                XmlUtils.beginDocument(parser, "root");
                final int depth = parser.getDepth();
                int type;
                while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }
                    final String name = parser.getName();
                    if(!"ChannelType".equals(name)){
                        continue;
                    }
                    ChannelType ct = new ChannelType();
                    for (int j = 0; j < parser.getAttributeCount(); j++) {
                        if(parser.getAttributeName(j).equals(ChannelType.BRABDS_KEY)){
                            ct.setBrands(parser.getAttributeValue(j));
                        }else if(parser.getAttributeName(j).equals(ChannelType.LEAF_KEY)){
                            ct.setLeaf(parser.getAttributeValue(j));
                        }else if(parser.getAttributeName(j).equals(ChannelType.NAME_KEY)){
                            ct.setName(parser.getAttributeValue(j));
                        }else if(parser.getAttributeName(j).equals(ChannelType.Category_KEY)){
                            ct.setCategory(parser.getAttributeValue(j));
                        }else if(parser.getAttributeName(j).equals(ChannelType.CHANNEL_KEY)){
                            ct.setChannel(parser.getAttributeValue(j));
                        }else if(parser.getAttributeName(j).equals(ChannelType.PARENT_KEY)){
                            ct.setParent(parser.getAttributeValue(j));
                        }else{
                            Log.w(TAG, "解析异常");
                        }
                    }
                    Log.i("hejianfeng", "解析完节点"+ct.toString());
                    mList.add(ct);
                }
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch (IOException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            }
            return mList;
        }
        
        public void setCategory(String category){
            this.mCategory =category;
        }
        public List<ChannelType> getChannels(){
        	return mList;
        }
    }
    
    public List<ChannelType> enqueue(Query query,String currentBrands) {
        return query.getChannels();
    }
}
