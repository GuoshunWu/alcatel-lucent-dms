/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-9
 * Time: 下午7:48
 * To change this template use File | Settings | File Templates.
 */

({
    appDir:"../",
    baseUrl:"modulejs/lib",
    dir:"../../webapp-build",
    //Comment out the optimize line if you want
    //the code minified by UglifyJS.
    optimize:"none",

    paths:{
        jquery:"../require-jquery",
		appmng:'../appmng'
    },

    modules:[
        {
            name:"appmng",
            exclude:['coffee-script']
        }
    ]
})