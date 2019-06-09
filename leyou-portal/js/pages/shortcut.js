const shortcut = {
    template: "\
    <div class='py-container'> \
        <div class='shortcut'> \
            <ul class='fl'> \
               <li class='f-item'>Welcome！</li> \
               <li class='f-item' v-if='user && user.username'>\
               Dear <span style='color: red;'>{{user.username}}</span>\
               </li>\
               <li v-else class='f-item'> \
                   Please <a href='javascript:void(0)' @click='gotoLogin'>Login</a>　 \
                   <span><a href='/register.html' target='_blank'>Register</a></span> \
               </li> \
           </ul> \
           <ul class='fr'> \
               <li class='f-item'><a href='/home.html' target='_blank'>order</a></li> \
               <li class='f-item space'></li> \
               <li class='f-item'>member</li> \
               <li class='f-item space'></li> \
               <li class='f-item' id='service'> \
                   <span>service</span> \
                   <ul class='service'> \
                       <li><a href='/cooperation.html' target='_blank'>合作招商</a></li> \
                       <li><a href='/shoplogin.html' target='_blank'>商家后台</a></li> \
                       <li><a href='/cooperation.html' target='_blank'>合作招商</a></li> \
                       <li><a href='#'>商家后台</a></li> \
                   </ul> \
               </li> \
           </ul> \
       </div> \
    </div>\
    ",
    name: "shortcut",
    data() {
        return {
            user: null
        }
    },
    created() {
        ly.http("/auth/verify")
            .then(resp => {
                this.user = resp.data;
            })
    },
    methods: {
        gotoLogin() {
            window.location = "/login.html?returnUrl=" + window.location;
        }
    }
}
export default shortcut;