package com.example.newcommunity.util;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component//交给spring管理
public class SensetiveFilter {

    private static final Logger logger= LoggerFactory.getLogger(SensetiveFilter.class);

    //检测到敏感词后把敏感词替换成什么样的常量
    private static final String REPLACEMENT="***";

    //根节点
    private TrieNode rootNode=new TrieNode();

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct//在初始化这个对象之后调用
    public void init(){
        try {
            // 使用Spring的ResourceLoader加载资源，更可靠
            Resource resource = resourceLoader.getResource("classpath:sensetive.words.txt");
            if (!resource.exists()) {
                logger.error("敏感词文件未找到: classpath:sensetive.words.txt");
                return;
            }

            try (InputStream is = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))
            ) {
                String keyword;
                while ((keyword = reader.readLine()) != null) {
                    //添加到前缀树
                    this.addKeyword(keyword);
                }
                logger.info("敏感词加载成功");
            }
        } catch (Exception e) {
            logger.error("加载敏感词失败", e);
        }
    }

    //将一个敏感词添加到前缀树里面(根据敏感词初始化敏感词前缀树)
    private void addKeyword(String keyword){
        TrieNode node=rootNode;
        for(int i=0;i<keyword.length();i++){
            char c=keyword.charAt(i);
            if(node.getSubNode(c)==null){
                node.addSubNode(c,new TrieNode());
            }
            node=node.getSubNode(c);
        }
        node.setKeywordEnd(true);
    }

    //公有方法会被外界调用,返回过滤之后的字符串
    public String filter(String text){
        if(!StringUtils.hasText(text)){
            return text;
        }
        StringBuilder sb=new StringBuilder();
        //声明三个指针，指向根节点，指向正在遍历的字符串第一位，最后一位
        TrieNode tempNode=rootNode;
        int begin=0;
        int position=0;
        while(position<text.length()){
            char c=text.charAt(position);
            //跳过符号
            if(isSymbol(c)){
                //将此符号计入结果，让指针2往下走
                if(tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode=tempNode.getSubNode(c);
            if(tempNode==null){
                //下一级没有节点,以begin开头的字符串没有敏感词
                sb.append(text.charAt(begin));
                position=++begin;
                tempNode=rootNode;
            }else if(tempNode.isKeywordEnd()){
                sb.append(REPLACEMENT);
                begin=++position;
                tempNode=rootNode;
            }else{
                position++;
            }
        }
        // 将最后一段字符加入结果
        if(begin < text.length()){
            sb.append(text.substring(begin));
        }
        return sb.toString();
    }


    //判断是否为符号
    private boolean isSymbol(char c){
        //东亚文字范围（中文日文韩文什么的）
        return !Character.isLetterOrDigit(c) && (c < 0x2E80 || c > 0x9FFF);
    }



    //前缀树
    private class TrieNode{
        //描述关键词结束的标识
        private boolean isKeywordEnd=false;
        //子节点（每个字符对应一个子节点，子节点的key是字符，value是子节点）？？？
        private Map<Character,TrieNode> subNodes=new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keyworEnd) {
            isKeywordEnd = keyworEnd;
        }

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);//实际返回的是map?因为trienode对象里面的属性subNodes是一个map，所以返回的是map
        }

    }
}