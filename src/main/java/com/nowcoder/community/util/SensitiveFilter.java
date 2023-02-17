package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";    // 遇到敏感词替换的字符

    //根节点
    private TireNode rootNode = new TireNode();

    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("Sensitive-words.txt");   // 从类加载器中获取
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));  // 转换成缓冲流
        ){
            String keyWord;
            while ((keyWord = reader.readLine()) != null){
                // 如果读到了，将敏感词添加到前缀树中
                this.addKeyWord(keyWord);
            }
        } catch (IOException e){
            logger.error("加载敏感词失败:" + e.getMessage());
        }

    }


    // 将一个敏感词添加到前缀树
    private void addKeyWord(String keyWord){
        TireNode tempNode = rootNode;   // 默认指向根
        for (int i = 0; i < keyWord.length(); i++){
            char c = keyWord.charAt(i);
            TireNode subNode = tempNode.getSubNode(c);

            // 当前子节点不存在时新建子节点，否则直接使用
            if (subNode == null){
                // 初始化子节点
                subNode = new TireNode();
                tempNode.addSubNode(c,subNode);
            }
            // 指针指向子节点进入下一层
            tempNode = subNode;

            // 设置结束的标识
            if (i == keyWord.length() - 1){
                tempNode.setKeyWordEnd(true);   // 标识敏感词结束
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 带有敏感词的文本
     * @return  过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }

        // 指针1
        TireNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;

        // 不断记录追加的过程
        StringBuilder sb = new StringBuilder();

        while (position < text.length()){
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)){
                // 若指针1指向根节点，将此符号计入结果，让指针2向下走
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头还是中间，指针3都向下走一步
                position++;
                continue;
            }
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                // begin为开头字符串不是敏感词
                sb.append(text.charAt(begin));
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeyWordEnd()){
                // 发现敏感词，将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
            } else {
                // 继续检查下一个字符
                if(position<text.length()-1){
                    position++;
                } else {
                    sb.append(text.charAt(begin));
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = rootNode;
                }
            }
        }
        return sb.toString();
    }


    // 判断是否为符号
    // CharUtils.isAsciiAlphanumeric() 遇到特殊字符为false，所以取反
    // 0x2E80 - 0x9FFF是东亚文字范围，在此之外可以认为是特殊符号
    private boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c >0x9FFF);
    }

    // 构建前缀树(内部类)
    private class TireNode{
        // 关键字结束的标识
        private boolean isKeyWordEnd = false;

        // map中k是下级节点的字符，v是下级节点
        private Map<Character,TireNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character character, TireNode node){
            subNodes.put(character,node);
        }

        // 获取子节点
        public TireNode getSubNode(Character character){
            return subNodes.get(character);
        }
    }
}
