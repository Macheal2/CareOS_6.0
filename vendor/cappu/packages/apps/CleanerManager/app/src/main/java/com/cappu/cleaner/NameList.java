package com.cappu.cleaner;

/**
 * Created by hmq on 17-2-20.
 */

public class NameList {
    public static String[] ADFileList = new String[]{
            ".wbadcathe",           //新浪广告
            "MSDK",                 //腾讯广告
            "Tencent/tbs/tbslog",   //TBS
            "baidu/tempdata",       //百度临时文件
            "joyCache",             //Mobappbox广告
            //5
            ".BD_SAPI_CACHE",       //百度推广日志
            "wostoresecuriry",      //Wostoresecurity广告
            "UxinSDK",              //Uxin SDK Logs
            "Tencent/Tpush",        //腾讯推送日志
            ".SystemConfig",        //阿里网页广告
            //10
            ".com.taobao.dp",       //淘宝网页广告
            "Tencent/TMAssistantSDK/Logs",//腾讯日志
            ".UTSystemConfig",      //可清除淘宝文件
            "backups/.SystemConfig" //阿里网页广告
    };

    public static String[] ADNameList = new String[]{
            "新浪广告",
            "腾讯广告",
            "Useless Logs",
            "百度临时文件",
            "Mobappbox广告",
            //5
            "百度推广日志",
            "Wostoresecurity广告",
            "Uxin SDK Logs",
            "腾讯推送日志",
            "阿里网页广告",
            //10
            "淘宝网页广告",
            "腾讯日志",
            "可清除淘宝文件",
            "阿里网页广告"
    };

    public static String[] WechatFileList = new String[]{
            "CheckResUpdate",   //检查更新缓存
            "xlog",             //监控日志
            "crash",            //崩溃日志
            "vusericon",        //星标用户图标
            "Handler",          //日志文件
            //5
            "SQLTrace",         //跟踪文件
            "openapi",          //游戏中心图标
            "diskcache",        //缓存
            "wallet"            //银行卡图标
    };

    public static String[] WechatNameList = new String[]{
            "检查更新缓存",
            "监控日志",
            "崩溃日志",
            "星标用户图标",
            "日志文件",
            //5
            "跟踪文件",
            "游戏中心图标",
            "缓存",
            "银行卡图标"
    };
}
