var RevSlide = function() {
	return {
		initRevolutionSlider : function() {
			var api;
			api = jQuery('.fullwidthabnner').revolution({
				delay : 2000,
				startheight : 600,
				startwidth : 1150,
				hideThumbs : 10,
				thumbWidth : 100,
				thumbHeight : 50,
				thumbAmount : 5,
				navigationType : "bullet",
				navigationArrows : "solo",
				navigationStyle : "round",
				navigationHAlign : "center",
				navigationVAlign : "bottom",
				navigationHOffset : 0,
				navigationVOffset : 20,
				soloArrowLeftHalign : "left",
				soloArrowLeftValign : "center",
				soloArrowLeftHOffset : 20,
				soloArrowLeftVOffset : 0,
				soloArrowRightHalign : "right",
				soloArrowRightValign : "center",
				soloArrowRightHOffset : 20,
				soloArrowRightVOffset : 0,
				touchenabled : "on",
				onHoverStop : "on",
				stopAtSlide : -1,
				stopAfterLoops : -1,
				hideCaptionAtLimit : 0,
				hideAllCaptionAtLilmit : 0,
				hideSliderAtLimit : 0,
				shadow : 1,
				fullWidth : "on"
			})
		}
	}
}();