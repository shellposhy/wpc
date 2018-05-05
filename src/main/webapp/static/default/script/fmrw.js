$(".topnav a").each(function(index,obj){
	if($(this).html()=="#" || $(this).html()==""){
		$(this).hide();
	}
})

$(window).load(function(){
  var fixedArrNum=[0];
	$("#container dfn").each(function(){
		fixedArrNum.push($(this).offset().top) 
	})
	
	$(".topnav a").each(function(index,obj){
		if(index)
		$(this).bind("click",function(){
			$(".topnav a").removeClass("on");
			$(obj).addClass("on")
			$("html,body").stop().animate({scrollTop:fixedArrNum[index]},420);
		})
	})
	$(window).scroll(function(){
		$.each(fixedArrNum,function(index,obj){
			if($(document).scrollTop() >= obj ){
				$(".topnav a").removeClass("on");
				$(".topnav a").eq(index).addClass("on")
			}
		})
	})
		
});
