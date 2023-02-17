$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// 获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	// 发送异步请求
	$.post(
		CONTEXT_PATH  + "/discuss/add",       			// 访问路径
		{"title":title,"content":content},             // 向服务器提交的数据
		function (data) {                     			// 回调函数 date -> 服务器返回给浏览器的字符串
			// 需要将字符串转换为js对象
			data = $.parseJSON(data);
			// 在提示框中显示返回的消息
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");
			// 2秒后自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 刷新页面
				if (data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	);
}