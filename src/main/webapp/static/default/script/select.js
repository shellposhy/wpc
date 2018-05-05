function myFun(obj) {
    if(obj.src.indexOf("images/checkedoff.jpg")!=-1){
	    obj.src = 'images/checkedon.jpg';
	    document.LogonForm.isRemember.value="1";
    }else{
        obj.src = 'images/checkedoff.jpg';
	    document.LogonForm.isRemember.value="0";
    }
}