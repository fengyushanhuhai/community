package com.nowcoder.community.entity;

/**
 * 封装分页的信息
 */
public class Page {

    // 页面传入的信息
    private int current = 1;    // 页面传入当前页码，默认值为1
    private int limit = 10;     // 页面传入最多显示的多少条数据上限，默认是10

    // 自己查询的信息
    private int rows;           // 一共有多少条数据，用于计算总的页数 rows / limit
    private String path;        // 查询路劲，用来复用分页的链接

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {   // 一页最多limit条数据
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // 数据库查询时所需要的

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffset(){
        return current * limit - limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal(){
        // 能否整除 ==> 页数是否加1
        if(rows % limit == 0){
            return rows / limit;
        }else{
            return rows / limit + 1;
        }
    }

    /**
     * 获取起始页码 当前页码 - 2
     * @return
     */
    public int getFrom(){
        int from = current - 2;
        return from < 1 ? 1 : from;         // 防止前面页码为0或者负值
    }

    /**
     * 获取结束页码 当前页码 + 2
     * @return
     */
    public int getTo(){
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;     // 防止后面页码大于最大页码
    }
}
