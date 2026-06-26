$(function(){
	$("#publishBtn").click(publish);
});
console.log("POST URL =", CONTEXT_PATH + "/discuss/add");
console.log("publishBtn =", $("#publishBtn").length);
function publish() {
	$("#publishModal").modal("hide");
	//获取标题和内容
	var title=$("#title").val();
	var content=$("#content").val();
	
	// 验证输入
	if(!title || !content) {
		$("#hintBody").text("标题和内容不能为空！");
		$("#hintModal").modal("show");
		setTimeout(function(){
			$("#hintModal").modal("hide");
		}, 2000);
		return;
	}
	
	//发送异步请求
	$.post(
		CONTEXT_PATH+"/discuss/add",
		{
			"title":title,"content":content,"type":0
		},
		function(data){
			try {
				// 尝试解析JSON，但如果后端直接返回JSON字符串，则不需要再次解析
				if(typeof data === 'string') {
					data = $.parseJSON(data);
				}
				//在提示框中显示返回消息
				$("#hintBody").text(data.msg);
				$("#hintModal").modal("show");
				//两秒后自动隐藏
				setTimeout(function(){
					$("#hintModal").modal("hide");
					//刷新页面
					if(data.code === 200){
						window.location.reload();
					}
				}, 2000);
			} catch(e) {
				console.log("JSON解析错误:", e);
				console.log("原始响应:", data);
				$("#hintBody").text("响应格式错误，请联系管理员");
				$("#hintModal").modal("show");
				setTimeout(function(){
					$("#hintModal").modal("hide");
				}, 2000);
			}
		}
	).fail(function(xhr, status, error){
		console.log("请求失败:", xhr.responseText, status, error);
		var errorMsg = "请求失败，请稍后重试";
		if(xhr.responseJSON && xhr.responseJSON.msg) {
			errorMsg = xhr.responseJSON.msg;
		} else if(xhr.responseText) {
			errorMsg = xhr.responseText;
		}
		$("#hintBody").text(errorMsg);
		$("#hintModal").modal("show");
		setTimeout(function(){
			$("#hintModal").modal("hide");
		}, 2000);
	});
}