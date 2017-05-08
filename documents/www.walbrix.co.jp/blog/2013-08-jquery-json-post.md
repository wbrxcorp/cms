---
title: jQueryでJSONをPOSTしてJSONのレスポンスを受け取る
summary: よくある要件だと思うんだけど、検索してもHTTPの仕様（主に上りと下りそれぞれのcontent-typeのこと）についてよく理解した上で書かれていそうな良い情報が上位に出てこないので自分用にメモしておく。
date: 2013-08-16
page_image: /blog/javascript.jpg
template: www.walbrix.co.jp/page.html.j2
jumbotron_class: simple
adsense: true
---
<form class="form-horizontal">
  <div class="control-group">
    <label class="control-label" for="name">お名前</label>
    <div class="controls">
      <input id="name" type="text" value="山田 太郎"/>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="age">年齢</label>
    <div class="controls">
      <input id="age" type="text" value="36"/>
    </div>
  </div>
  <div class="control-group">
    <div class="controls">
      <button id="update" type="button">更新</button>
    </div>
  </div>
</form>

```
<form class="form-horizontal">
  <div class="control-group">
    <label class="control-label" for="name">お名前</label>
    <div class="controls">
      <input id="name" type="text" value="山田 太郎"/>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="age">年齢</label>
    <div class="controls">
      <input id="age" type="text" value="36"/>
    </div>
  </div>
  <div class="control-group">
    <div class="controls">
      <button id="update" type="button">更新</button>
    </div>
  </div>
</form>
```

上記フォームの「更新」ボタンを押したとき、ブラウザ上で画面遷移を発生させずに下記のようなJSONをサーバへPOSTしたい。

```
{
    "name":"山田 太郎",
    "age":36
}
```

サーバーは、処理が成功した場合

```
[true]
```

処理が失敗した場合

```
[false, "エラーの理由"]
```

のようなレスポンスをこれまた JSONで返す。

下記のような JavaScriptでサーバに JSONを送信する。当然、サーバ側は Content-Type: application/json の POSTを受け付けるようにプログラムされている必要がある。

```
$("button#update").click(function() {
    // 多重送信を防ぐため通信完了までボタンをdisableにする
    var button = $(this);
    button.attr("disabled", true);

    // 各フィールドから値を取得してJSONデータを作成
    var data = {
        name: $("#name").val(),
        age: parseInt($("#age").val())
    };

    // 通信実行
    $.ajax({
        type:"post",                // method = "POST"
        url:"/path/to/post",        // POST送信先のURL
        data:JSON.stringify(data),  // JSONデータ本体
        contentType: 'application/json', // リクエストの Content-Type
        dataType: "json",           // レスポンスをJSONとしてパースする
        success: function(json_data) {   // 200 OK時
            // JSON Arrayの先頭が成功フラグ、失敗の場合2番目がエラーメッセージ
            if (!json_data[0]) {    // サーバが失敗を返した場合
                alert("Transaction error. " + json_data[1]);
                return;
            }
            // 成功時処理
            location.reload();
        },
        error: function() {         // HTTPエラー時
            alert("Server Error. Pleasy try again later.");
        },
        complete: function() {      // 成功・失敗に関わらず通信が終了した際の処理
            button.attr("disabled", false);  // ボタンを再び enableにする
        }
    });
});
```
