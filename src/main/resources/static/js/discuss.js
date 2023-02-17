function like(btn, entityType, entityId, entityUserId) {
    // 向服务器发送异步请求
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId},
        function (data) {
            // 转变成json数据
            data = $.parseJSON(data);
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':"赞");
            if (data.code == 0){

            }else {
                alert(data.msg);
            }
        }
    );
}