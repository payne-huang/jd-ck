<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <link href="/static/css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
    <link href="/static/css/font-awesome.css?v=4.4.0" rel="stylesheet">
    <link href="/static/css/plugins/iCheck/custom.css" rel="stylesheet">
    <link href="/static/css/animate.css" rel="stylesheet">
    <link href="/static/css/style.css?v=4.1.0" rel="stylesheet">
    <link href="/static/css/plugins/sweetalert/sweetalert.css" rel="stylesheet">
    <link href="/static/css/plugins/bootstrap-table/bootstrap-table.min.css" rel="stylesheet">
</head>
<body>
<div class="wrapper wrapper-content animated fadeInUp">

    <div class="ibox-content">
        <div style="position: center;border-color: #010101">

            CK: <input id="pin" type="text" style="width: 50%;position: center">
            备注: <input id="comment" type="text" style="width: 15%;position: center">
            <input type="submit" value="更新" onclick="updateCK()">
        </div>
    </div>
    <div class="ibox-content">
        <div class="table-responsive">
            <table id="ckTable" class="table table-hover"></table>
        </div>
    </div>
</div>

<!-- 全局js -->
<script src="/static/js/jquery.min.js?v=2.1.4"></script>
<script src="/static/js/bootstrap.min.js?v=3.3.6"></script>
<!-- 自定义js -->
<script src="/static/js/bootstrap-table/bootstrap-table.min.js"></script>
<script src="/static/js/bootstrap-table/locale/bootstrap-table-zh-CN.min.js"></script>

<script>

    $(window).resize(function () {
        $('#ckTable').bootstrapTable('resetView', {
            height: tableHeight()
        })
    });

    $('#ckTable').bootstrapTable({
        method: 'get',
        contentType: "application/json; charset=utf-8",//必须要有！！！！
        dataType: "json",//返回json格式的数据
        url: "/ck/list",//要请求数据的文件路径
        height: tableHeight,//高度调整
        ajaxOptions: {
            headers: {
                // "Authentication": $.cookie("Authentication")
            }
        },
        toolbar: '#toolbar',//指定工具栏
        striped: true, //是否显示行间隔色
        idField: "id",
        dataField: "list",//bootstrap table 可以前端分页也可以后端分页，这里
        //我们使用的是后端分页，后端分页时需返回含有total：总记录数,这个键值好像是固定的
        //rows： 记录集合 键值可以修改  dataField 自己定义成自己想要的就好
        // queryParams: queryParams,//请求服务器时所传的参数
        showRefresh: false,//刷新按钮
        showColumns: false,
        search: false,
        clickToSelect: true,//是否启用点击选中行
        toolbarAlign: 'right',//工具栏对齐方式
        buttonsAlign: 'right',//按钮对齐方式
        toolbar: '#toolbar',//指定工作栏
        columns: [
            {
                title: 'PIN',
                field: 'value'
            },
            {
                title: '备注',
                field: 'remarks',
            },
            {
                title: '状态',
                field: 'status',
                formatter: statusFormatter
            }
        ],
        locale: 'zh-CN',//中文支持,
        responseHandler: function (res) {
            return res.content;
        }
    });

    function statusFormatter(value, row, index) {
        switch (value) {
            case 0:
                return "<font color='#5cb85c'>启用</font>";
            case 1:
                return "<font color='#d9534f'>禁用</font>"
        }
    }

    function tableHeight() {
        return $(window) - 280;
    }

    function updateCK() {
        var data = "{";
        data = data + "\"pin\":\"" + $("#pin").val() + "\",";
        data = data + "\"comment\":\"" + $("#comment").val() + "\"";
        data = data + "}";
        $.ajax({
            type: "post",
            dataType: "json",
            url: "/ck/put",
            async: false,
            data: data,
            contentType: "application/json; charset=utf-8",
            success: function (msg) {//msg为返回的数据，在这里做数据绑定
                if (msg.status == "ok") {
                    alert("更新成功！");
                    window.location.href = "/index"
                } else {
                    swal({
                        title: "提示！",
                        text: "更新失败!，请联系管理员！",
                        type: "error"
                    });
                }
            }
        });
    }
</script>
</body>
</html>
