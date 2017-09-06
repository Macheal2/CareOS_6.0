#!/usr/bin/python
#coding=utf-8

import types
import sys,os
import shutil
CurDir = os.getcwd()
print '当前的路径',CurDir

class ResolveXML():
    def __init__(self,xmlPath):
        
        self.XMLFile = self.cur_file_dir()+"/"+xmlPath
        print '+++++++++',self.XMLFile
    
    '''
    这个是查找String.xml文件的方法
    
    nodeName           节点名字
    attribute          节点的属性
    attributeValues    属性的值
    
    '''
    def ParseXML(self,nodeName,attribute,attributeValues):
        values = []
        #encoding=utf-8
        from xml.etree import ElementTree as ET
        #要找出所有人的年龄
        per=ET.parse(self.XMLFile)
        p=per.findall(nodeName)
        #print '\n'
        #print '节点查找结果',p
        #print '\n'
        '''
        for x in p:
            print '59',x.attrib #这里找到的是节点下的属性 以及 属性的值 这里是一个字典
        '''
        for oneper in p:  #找出 person节点
            if oneper.attrib[attribute] == attributeValues:
                #print '找到了想要的文件',oneper,'      ',type(oneper),'--------'
                #print '~~~~~~~~~~',oneper.text,'~~~~~~~~~~'
                if type(oneper) == types.InstanceType:#如果结果是一个文本类型
                    return oneper.text
                else:  #如果结果是一个list类型
                    for child in oneper.getchildren(): #找出person节点的子节点
                        print child.tag,':',child.text
                        values.append(child.text)
             
                #print 'item:',oneper.get('item')
                #print '############'
        return values
    #获取脚本文件的当前路径
    def cur_file_dir(self):
        #获取脚本路径
        path = sys.path[0]
        #判断为脚本文件还是py2exe编译后的文件，如果是脚本文件，则返回的是脚本的目录，如果是py2exe编译后的文件，则返回的是编译后的文件路径
        if os.path.isdir(path):
            print "path:",path
            return path
        elif os.path.isfile(path):
            print "path:",path
            return os.path.dirname(path)
    
    
def copyFiles(sourceDir,  targetDir): #把某一目录下的所有文件复制到指定目录中   sourceDir这个是要拷贝的目录  targetDir这个是存放的目录
    if sourceDir.find(".git") > 0: 
        return 
    for file in os.listdir(sourceDir): 
        sourceFile = os.path.join(sourceDir,  file) 
        targetFile = os.path.join(targetDir,  file) 
        #print 'cp ',sourceFile,'  -->',targetFile
           
        if os.path.isfile(sourceFile): 
            shutil.copyfile(sourceFile, targetFile)
            '''
            #这里修改拷贝方式是因为下面的没有原始文件的权限问题
            if not os.path.exists(targetDir): 
                os.makedirs(targetDir) 
            if not os.path.exists(targetFile) or(os.path.exists(targetFile) and (os.path.getsize(targetFile) != os.path.getsize(sourceFile))): 
                    open(targetFile, "wb").write(open(sourceFile, "rb").read()) 
            '''
        if os.path.isdir(sourceFile): 
            First_Directory = False 
            if not os.path.exists(targetFile):
                os.mkdir(targetFile)
            self.copyFiles(sourceFile, targetFile) 
if __name__ == '__main__':
    if len(sys.argv)>1:
        xml = ResolveXML('res/values/defaults.xml')
        if(sys.argv[1] == 'start'):
            print '拷贝编译开始'
            values = xml.ParseXML('/integer/','name','launcher_version')
            print '当前桌面是 values:',values,'     当前的values数据类型:',type(values)
            
            if type(values) == types.StringType and values == '2':
                print '拷贝线上版本'
                copyFiles(xml.cur_file_dir()+"/maniXML/cappuOL/", xml.cur_file_dir())
            elif type(values) == types.StringType and values == '1':
                print '拷贝项目版本'
                copyFiles(xml.cur_file_dir()+"/maniXML/cappu/", xml.cur_file_dir())
            else:
                print '拷贝异常'
        elif (sys.argv[1] == 'end'):
            print '拷贝编译结束'
            copyFiles(xml.cur_file_dir()+"/maniXML/common/", xml.cur_file_dir())
                
                
    