---
category: ブログ
title: 64bit Windowsで 32bitのODBCドライバを使う
summary: 32bitのODBCドライバをインストールしたら、設定も32bitのODBCアドミニストレータでしなければいけないという話
date: 2007-12-04
page_image: /blog/windows.jpg
template: www.walbrix.co.jp/page.html.j2
jumbotron_class: simple
adsense: true
---
RDBMSにアクセスするためODBCドライバをインストールしたが、データソースを設定すべくコントロールパネル→管理ツール→ODBC を開いてもドライバ一覧に出てこない・・・64bit Windowsを使っているとそういうことが起こる。理由は簡単で、インストールしたODBCドライバが64bit版じゃないから。

そういう時は 32bit版の ODBCアドミニストレータを実行してやると良い。

```
C:¥WINDOWS¥SysWOW64¥odbcad32.exe
```

<img src="2007-12-64bit-windows-32bitodbc.png" class="img-fluid img-thumbnail">&nbsp;

これで設定すれば、32bitアプリケーションからODBCデータソースを利用できる。
