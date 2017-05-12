---
title: AngularJSでselect要素にoptionをぶら下げる色々な方法
summary: AngularJSではselect要素による選択フィールドもスコープ変数にバインドすることが出来る。その際、option要素の代わりに ng-options属性で選択肢を提供するための書式を解説する。
date: 2014-02-13
page_image: /blog/img/angularjs.png
template: www.walbrix.co.jp/page.html.j2
jumbotron_class: simple
adsense: true
---
{%raw%}
ng-options属性の書式は一見独特で最初はわかりにくいので、具体的な例をいくつか挙げることで理解の助けにしてもらおうと思う。

公式ドキュメント: [AngularJS: API: select](http://docs.angularjs.org/api/ng/directive/select)

## 数値や文字列などの配列から選択させる

配列にプリミティブな値が直接格納されており、選択肢のラベル＝バインドされる値で構わない場合には一番シンプルな記述となる。

### JavaScript

```
$scope.years = [2010,2011,2012,2013,2014];
$scope.selectedYear = 2014;
```

### HTML

```
<select ng-model="selectedYear" ng-options="year for year in years"></select>
選択された値 = {{ selectedYear }}
```

<div class="row">
  <div class="col-xs-6"><img src="/blog/img/select01.png" class="img-fluid img-thumbnail"></div>
  <div class="col-xs-6"><img src="/blog/img/select02.png" class="img-fluid img-thumbnail"></div>
</div>

### ng-optionsの解説

<table class="table">
  <tr><th>バインドされ、かつラベルとなる値</th><th>for 各要素を表す変数名</th><th>in 配列名</th></tr>
  <tr>
    <td><span class="blue">year</span></td><td><strong>for</strong> <span class="blue">year</span></td>
    <td><strong>in</strong> years</td>
  </tr>
</table>

## 「未選択状態」を設ける

デフォルトの選択肢が存在せず、初期状態として「選択して下さい」のようなラベルを持った「未選択状態」をあらわす optionを持たせたい時は、バインドするスコープ変数を nullなりで初期化しておいた上で value="" を持った option要素を select要素内にひとつ配置する。

### JavaScript

```
$scope.selectedYear = null;
```

### HTML

```
<select ng-model="selectedYear" ng-options="year for year in years">
    <option value="">選択してください</option>
</select>
```

AngularJSでは select要素にも required属性を付けることが出来る。required属性のついたセレクトボックスが未選択状態の時、それを囲むフォームの $valid が falseになるため、その状態では続行のためのボタンを押せないようにするなどしてユーザーに選択を強制することができる。

```
<form name="yearForm">
  <select ng-model="selectedYear" ng-options="year for year in years" required>
    <option value="">選択してください</option>
  </select>
  <button ng-disabled="!yearForm.$valid">OK</button>
</form>
```

<div class="row">
  <div class="col-xs-6"><img src="/blog/img/select03.png" class="img-fluid img-thumbnail"></div>
  <div class="col-xs-6"><img src="/blog/img/select04.png" class="img-fluid img-thumbnail"></div>
</div>&nbsp;

## オブジェクトの配列から選択させる

$resourceサービスの query 関数でAPIサーバから取得したデータはオブジェクトの配列なので、これを直接 select要素の選択肢リストとして使えると便利だ。

### JavaScript

```
$scope.months = [
  {num:1,label:"January"}, {num:2,label:"February"}, {num:3,label:"March"},
  {num:4,label:"April"}, {num:5,label:"May"}, {num:6,label:"June"},
  {num:7,label:"July"}, {num:8,label:"August"}, {num:9,label:"September"},
  {num:10,label:"October"}, {num:11,label:"November"}, {num:12,label:"December"}
];
$scope.selectedMonth = $scope.months[0];
```

### HTML

```
<select ng-model="selectedMonth" ng-options="month as month.num + '月' for month in months"></select>
選択された値 = {{ selectedMonth }}
```

古典的なサーバ主導の Webアプリケーションだと select要素で選択できる値は文字列だけだが、AngularJSでは選択されたオブジェクトをまるごとスコープ変数にバインドすることが出来る。

<div class="row">
  <div class="col-xs-6"><img src="/blog/img/select05.png" class="img-fluid img-thumbnail"></div>
  <div class="col-xs-6"><img src="/blog/img/select06.png" class="img-fluid img-thumbnail"></div>
</div>&nbsp;

### ng-optionsの解説

<table class="table">
  <tr><th>バインドされる値</th><th>as ラベルとなる値</th><th>for 各要素を表す変数名</th><th>in 配列名</th></tr>
  <tr>
    <td><span class="red">month</span></td><td><strong>as</strong> <span class="red">month</span>.num + '月'</td>
    <td><strong>for</strong> <span class="red">month</span></td><td><strong>in</strong> months</td>
  </tr>
</table>

## オブジェクトのプロパティをバインドする

オブジェクトそのものではなく、オブジェクトのプロパティをスコープ変数にバインドすることもできる。

### HTML

```
<select ng-model="selectedMonth" ng-options="month.num as month.label for month in months"></select>
選択された値 = {{ selectedMonth }}
```

<img src="/blog/img/select07.png" class="img-fluid img-thumbnail">&nbsp;

### ng-optionsの解説

<table class="table">
  <tr><th>バインドされる値</th><th>as ラベルとなる値</th><th>for 各要素を表す変数名</th><th>in 配列名</th></tr>
  <tr>
    <td><span class="red">month</span>.num</td><td><strong>as</strong> <span class="red">month</span>.label</td>
    <td><strong>for</strong> <span class="red">month</span></td><td><strong>in</strong> months</td>
  </tr>
</table>

## オブジェクトのプロパティから選択させる

ng-optionsのソースは配列でなくオブジェクトでも良い。但し、後述するがこの方法を使える局面は限られている。

### JavaScript

```
$scope.jmonths = {
  "睦月":1,	"如月":2,	"弥生":3,	"卯月":4,
  "皐月":5,	"水無月":6,	"文月":7,	"葉月":8,
  "長月":9,	"神無月":10,	"霜月":11,	"師走":12
};
$scope.selectedMonth = 1;
```

### HTML

```
<select ng-model="selectedMonth" ng-options="num as name for (name,num) in jmonths"></select>
選択された値 = {{ selectedMonth }}
```

<img src="/blog/img/select08.png" class="img-fluid img-thumbnail">&nbsp;

### ng-optionsの解説

<table class="table">
  <tr><th>バインドされる値</th><th>as ラベルとなる値</th><th>for (keyの変数名,valueの変数名)</th><th>in オブジェクト名</th></tr>
  <tr>
    <td><span class="blue">num</span></td><td><strong>as</strong> <span class="red">name</span></td>
    <td><strong>for</strong> (<span class="red">name</span>,<span class="blue">num</span>)</td>
    <td><strong>in</strong> jmonths</td>
  </tr>
</table>

jmonthsオブジェクトと上のスクリーンショットを見比べてみてほしい。順番がバラバラになっている。配列の要素と違いオブジェクトのプロパティは順番を任意にコントロールできないためこのようなことになってしまう。（JavaScriptオブジェクトのプロパティを列挙した際にどういう順序になるかは実装任せのようである）

選択肢の表示順がランダムで構わないシチュエーションというのは滅多にないはずなので、結論としてこの書式は使い道がない。

参考 [AngularJSでselect要素のデータバインド - Qiita](http://qiita.com/HamaTech/items/7209bb686650ae61b1eb)
{%endraw%}
