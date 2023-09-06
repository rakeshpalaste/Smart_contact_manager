console.log("This massage on Console")
//alert("Welcome To Site")


const toggleSidebar = () =>{
	console.log("Toggle function called");
	
    if($(".sidebar").is(":visible")){

        //true
        //band karna hai
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    }
    else{
        //false 
        //show karna hai
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%"); 
    }


};