const lyTop = {
    template: "\
    <div class='nav-top'> \
     <shortcut/>\
        <!--header--> \
        <div class='header' id='headApp'> \
            <div class='py-container'> \
                <div class='yui3-g Logo'> \
                    <div class='yui3-u Left logoArea'> \
                        <a class='logo-bd' title='ecommerce' href='index.html' target='_blank'></a> \
                    </div> \
                    <div class='yui3-u Center searchArea'> \
                        <div class='search'> \
                            <form action='' class='sui-form form-inline'> \
                                <!--searchAutoComplete--> \
                                <div class='input-append'> \
                                    <input type='text' id='autocomplete' v-model='key' \
                                           class='input-error input-xxlarge' @keydown.enter.stop.prevent='search'/> \
                                    <button @click='search' class='sui-btn btn-xlarge btn-danger' type='button'>search</button> \
                                </div> \
                            </form> \
                        </div> \
                        <div class='hotwords'> \
                            <ul> \
                                <li class='f-item'>hot item1</li> \
                                <li class='f-item'>hot item2</li> \
                                <li class='f-item'>hot item3</li> \
                                <li class='f-item'>hot item4</li> \
                                <li class='f-item'>hot item5</li> \
                                <li class='f-item'>hot item6</li> \
                                <li class='f-item'>hot item7</li> \
                            </ul> \
                        </div> \
                    </div> \
                    <div class='yui3-u Right shopArea'> \
                        <div class='fr shopcar'> \
                            <div class='show-shopcar' id='shopcar'> \
                                <span class='car'></span> \
                                <a class='sui-btn btn-default btn-xlarge' href='cart.html' target='_blank'> \
                                    <span>My Cart</span> \
                                    <i class='shopnum'>0</i> \
                                </a> \
                                <div class='clearfix shopcarlist' id='shopcarlist' style='display:none'> \
                                </div> \
                            </div> \
                        </div> \
                    </div> \
                </div> \
                <div class='yui3-g NavList'> \
                    <div class='yui3-u Left all-sort'> \
                        <h4>Hottest Goods</h4> \
                    </div> \
                    <div class='yui3-u Center navArea'> \
                        <ul class='nav'> \
                            <li class='f-item'>Goods</li> \
                            <li class='f-item'>Goods</li> \
                            <li class='f-item'>Goods</li> \
                            <li class='f-item'>Goods</li> \
                            <li class='f-item'>Goods</li> \
                            <li class='f-item'>Goods</li> \
                            <li class='f-item'></li> \
                            <li class='f-item'><a href='seckill-index.html' target='_blank'>seckill</a></li> \
                        </ul> \
                    </div> \
                    <div class='yui3-u Right'></div> \
                </div> \
            </div> \
        </div>\
       </div> \
      ",
    name:'ly-top',
    data() {
        return {
            key: "",
            query: location.search
        }
    },
    methods: {
        search() {
            window.location = 'search.html?key=' + this.key;
        },
        getUrlParam: function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) {
                return decodeURI(r[2]);
            }
            return null;
        }
    },
    created() {
        this.key = this.getUrlParam("key");
    },
    components: {
        shortcut:() => import('./shortcut.js')
    }
}
export default lyTop;