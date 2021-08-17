//$(document).ready(function(){
//  alert("jquery");
//});

// タイトルがクリックされたら削除アイテムに登録
$(document).on("click", ".item_title", function(){
    $(this).toggleClass("row_unselect");
    $(this).parent().toggleClass("row_unselect");
    $(this).parent().toggleClass("del_flg");
    $(this).toggleClass("row_selected");
    $(this).parent().toggleClass("row_selected");
});

// modalの表示する
$(document).on("click", ".item_modal_hook", function(){
    $(this).next().toggleClass("hidden");
});

// modal非表示にする
$(document).on("click", ".item_modal", function(){
    $(this).toggleClass("hidden");
});

// 発売日の更新
$(document).on("change", ".item_publication_date", function(){
    var original = $(this).find('input').attr('value');
    var updated = $(this).find('input').val();
    if (original != updated) {
        $(this).addClass("update_date");
        $(this).parent().addClass("update_date");
    } else {
        $(this).removeClass("update_date");
        $(this).parent().removeClass("update_date");
    }
});

// 更新ボタン押下時
$(document).on("click", "#update_btn", function(){
    // delするitem
    var del_items = [];
    $(".item_box.del_flg").each(function (i, elem) {
        del_items.push($(elem).data('id'));
    });
    console.log(del_items);

    // publication_dateを更新するitem
    var update_item = new Map();
    $(".item_box.update_date").each(function (i, elem) {
        var original = $(elem).find('input').attr('value');
        var updated = $(elem).find('input').val();
        if (original !== updated) {
            update_item.set($(elem).data('id'), updated);
        }
    });
    console.log(update_item);

    // データを処理に飛ばす(dlt)
    if (del_items.length > 0) {
        var basicInfo = JSON.stringify({
            items: del_items,
        });
        $.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json;charset=utf-8'
            },
            type:"POST",
            url:'/manage/dlt_flg',
            dataType: "json",
            data: basicInfo,
        }).done((data, textStatus, jqXHR) => {
            console.log(data);
            var idList = data.idList;
            data.idList.forEach(function(value) {
                var query = "[data-id='" + value + "']";
                $(document).find(query).each(function (i, elem) {
                    $(this).remove();
                });
            });
        })
    }

    // データを処理に飛ばす(update)
    if (update_item.size > 0) {
        $.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json;charset=utf-8'
            },
            type:"POST",
            url:'/manage/update_pd',
            dataType: "json",
            data: JSON.stringify(Array.from(update_item.entries()))
        }).done((data, textStatus, jqXHR) => {
            console.log(data);
            var idList = data.idList;
            data.idList.forEach(function(value) {
                var query = "[data-id='" + value + "']";
                $(document).find(query).each(function (i, elem) {
                    $(this).remove();
                });
            });
        });
    }
});

$(document).ready(function () {
    
})