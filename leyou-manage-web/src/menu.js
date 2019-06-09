var menus = [
  {
    action: "home",
    title: "Home",
    path:"/index",
    items: [{ title: "statistics", path: "/dashboard" }]
  },
  {
    action: "apps",
    title: "GoodsManagement",
    path:"/item",
    items: [
      { title: "Category", path: "/category" },
      { title: "Brand", path: "/brand" },
      { title: "GoodsList", path: "/list" },
      { title: "Specification", path: "/specification" }
    ]
  },
  {
    action: "people",
    title: "MemberManagement",
    path:"/user",
    items: [
      { title: "Statistics", path: "/statistics" },
      { title: "List", path: "/list" }
    ]
  },
  {
    action: "attach_money",
    title: "SalesManagement",
    path:"/trade",
    items: [
      { title: "Statistics", path: "/statistics" },
      { title: "Order", path: "/order" },
      { title: "Logistics", path: "/logistics" },
      { title: "Promotion", path: "/promotion" }
    ]
  },
  {
    action: "settings",
    title: "AuthorityManagement",
    path:"/authority",
    items: [
      { title: "List", path: "/list" },
      { title: "Role", path: "/role" },
      { title: "Member", path: "/member" }
    ]
  }
]

export default menus;
