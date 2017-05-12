---
category: ブログ
title: Zabbixを使ってエージェント無しで単純な Webサイト監視だけを行う最短の方法
summary: Zabbixを使ってサーバを監視するには原則として監視対象サーバに専用エージェントをインストールする必要があるが、単純な Webサイト監視であればエージェントをインストールしなくても出来るようになっている。
date: 2014-02-13
page_image: /blog/img/zabbix-simple-web-monitoring.jpg
template: www.walbrix.co.jp/page.html.j2
jumbotron_class: simple
adsense: true
---

<p><small>Zabbixは誰でも無料でインストールして利用できるオープンソースの強力なサーバー監視ツール。 <a href="http://www.zabbix.com/jp/">Zabbix日本語公式サイト</a></small></p>

Webサイトが正常に閲覧可能な状態かどうかだけひとまず監視したいという需要が世の中にはそれなりにあるはずなのだが、Zabbixのように高機能な監視システムをそういった単純な用途に使おうとして情報収集をしてもなかなかストレートに目的へ辿り着くことが出来ない。そこで、カジュアルなWeb監視用途に Zabbixを使おうとする人のための最短手順をまとめて公開することにした。

### エージェント監視とWeb監視

Zabbixエージェントを使った監視と Web監視の違いについて解説する。これらはどちらかだけ行うことも両方行うこともできるが、__この記事では後者のみを取り扱う__。

#### エージェントを使った監視

<img src="/blog/img/chart-agent-monitoring.png" class="img-fluid img-thumbnail">&nbsp;

Zabbixエージェントは監視対象サーバで稼働し続け、負荷状況などの詳細情報を収集する。これによって監視対象サーバの詳しい状況が手に取るようにわかるわけだが、

- 対象サーバに Zabbixエージェントデーモンをインストールして動作させる必要がある
- Zabbixサーバがエージェントと通信するためのポート（デフォルトでは10050番）を利用できるようファイアウォールに例外設定を追加する必要がある

のような要件が大人の事情その他によってクリアできない場合もあろうかと思う（後者にはエージェントをアクティブモードにして運用するという方法があるが）。

#### Web監視

Web監視(Web Monitoring) は、Zabbixサーバーが一般の Webサイト訪問者に扮して監視対象サーバにアクセスすることでサイトが正常に見られる状態かどうかをチェックする方式である。

<img src="/blog/img/chart-web-monitoring.png" class="img-fluid img-thumbnail">&nbsp;

エージェント監視と違って監視対象サーバの内部状態に関する情報まで収集することはできないが、この方式を利用した監視を行うだけであれば対象のサーバーに Zabbixエージェントをインストールする必要はない。

### Zabbixのインストール

・・・については本記事では取り扱わないので各自で済ませて頂きたい。当社で開発して無料で配布しているサーバー用オペレーティングシステム __[Walbrix (ワルブリックス)](https://www.walbrix.net)__ を適当な余っているパソコンに入れれば誰でも簡単に Zabbixを使い始められる</strong>ので、特に検証や学習の用途にお勧めしておく。

[Zabbix (サーバ監視) - 仮想アプライアンス - Walbrix](https://www.walbrix.net/va/zabbix.html)

### Zabbixへのログインとメニューの操作法

Zabbixのデフォルトユーザー名は <strong>admin</strong>、パスワードは <strong>zabbix</strong>となっている。実際の運用ではデフォルトの adminユーザーを無効にして実作業用のユーザーを別に作るべきだが、この記事では省略して adminユーザーのまま作業を続けることにする。

<div class="alert alert-warning">言うまでもないが実運用では少なくともパスワードだけは変えること</div>

<img src="/blog/img/login.png" class="img-fluid img-thumbnail">&nbsp;

Zabbixの管理画面では、画面左上にメインメニューとサブメニューが表示される。原則としてメインメニュー→サブメニューの順にクリックして作業を選択するのだが、メインメニューはクリックしなくてもマウスポインタをあてがうだけで選択された状態になる。おそらくこれはクリックの回数をなるべく減らそうという考えによるものだと思うが、慣れないうちは戸惑いやすいので注意してほしい。

<img src="/blog/img/menu.png" class="img-fluid img-thumbnail">&nbsp;

サブメニューの下に表示されているリンクの並びは今までに訪れたページの履歴であり、階層をナビゲーションするいわゆる「パンくずリスト」ではないことにも注意されたい。そうだと思って操作すると大変混乱することになる。

### アラートメール送信用のSMTP設定

Zabbixが障害を検知したとき、つまり監視対象の Webサイトが落ちた時には然るべき人員に通知（アラート）が送信されるのが望ましい。

Zabbixではアラートの送信媒体（メディア）として標準で Email, Jabber, SMSが用意されているが、ここでは最も簡単で身近な Emailを使うことにする。

メール送信の設定をするには「管理」「メディアタイプ」から Emailを選択する。

<img src="/blog/img/mediatype.png" class="img-fluid img-thumbnail">&nbsp;

Zabbixサーバからメールを送信させるにはSMTPサーバが必要である。Zabbixが動いているサーバがSMTPサーバを兼ねているのであれば（[Walbrix版Zabbix](https://www.walbrix.net/va/zabbix.html)はそのように設定されている）<strong>SMTPサーバー</strong>の欄には localhost を、そうでない場合は SMTPサーバーのホスト名を入力する。

<strong>SMTP helo</strong>欄にはSMTPサーバーに対して名乗る自分のドメイン名（自分が所有している正式なドメイン名を名乗ることが望ましいが特に無ければ localhostでも何でも良い）、<strong>送信元メールアドレス</strong>欄にはアラートメールの送信元（メールのFrom欄）として記載したいメールアドレスを入力し、保存ボタンをで設定を保存する。

<img src="/blog/img/mediatype-email.png" class="img-fluid img-thumbnail">&nbsp;

<div class="alert alert-warning">Zabbixは認証や暗号化を必要とするSMTPサーバとの通信に対応していないので、そのようなSMTPサーバを用いる必要がある場合はリレー用のSMTPサーバを設けるかカスタムスクリプトを作成する必要がある。</div>

### アラートメールの送信先設定

メールの送信方法を設定したら、次に送信先のアドレスを設定する。メニューの「管理」「ユーザー」から、Admin (Zabbix Administrator)ユーザーを選択してユーザー情報の編集画面を開く。

<img src="/blog/img/select-user.png" class="img-fluid img-thumbnail">&nbsp;

「メディア」タブから「追加」をクリックし、

<img src="/blog/img/add-media1.png" class="img-fluid img-thumbnail">&nbsp;

<strong>送信先</strong>欄にアラートメールの送信先となるメールアドレスを入力し、「追加」ボタンで追加する。<strong>有効な時間帯</strong>はデフォルトで 1-7(月曜日から日曜日)の00:00-24:00(つまり24時間いつでも)となっているので24時間365日いつでも障害があればメールが送信されることになる（ご愁傷様です）が、必要ならば変更する。

<img src="/blog/img/add-media2.png" class="img-fluid img-thumbnail">&nbsp;

<p>「メディア」にメールアドレスが追加されたことを確認したら「保存」ボタンを押して変更を確定する（ここで保存ボタンを押すのを忘れやすいので注意）。</p>

<img src="/blog/img/save-user.png" class="img-fluid img-thumbnail">&nbsp;

<p>ここまでで、「Zabbixがアラートメールを送信する方法」と「アラートのメールが送信される宛先」が設定された。</p>

<h3>アラート送信の有効化</h3>
<p>デフォルトでは、障害発生時にアラートを送信するアクションは無効になっているのでこれを有効にする。「設定」「アクション」「イベントソース=トリガー」で、<strong>"Report problems to Zabbix administrators"</strong> という名前のアクションを見つけ、デフォルトで「無効」になっているステータスを「有効」に切り替える。</p>

<img src="/blog/img/action.png" class="img-fluid img-thumbnail">&nbsp;

<p>ここまでで、「監視対象に何らかの障害があった場合には adminユーザーのメールアドレスにアラートメールが送信される」状態となった。</p>

<h3>監視対象ホストの追加</h3>

<p>アラート送信の設定が出来たところで、次に監視対象のホストを登録する。メニューから「設定」「ホスト」を辿り、「ホストの作成」ボタンを押す。</p>

<img src="/blog/img/create-host1.png" class="img-fluid img-thumbnail">&nbsp;

<p>「ホスト」タブで<strong>ホスト名</strong>（監視対象が http://www.walbrix.com/jp/ なら www.walbrix.com）、<strong>表示名</strong>（表示用の名前を任意に付けることが出来る）を入力し、<strong>所属グループ</strong>には Zabbixにデフォルトで用意されているグループから適当なものを選ぶ（例では Linux serversというグループを選んでいるが、グループ名はただの名前でしかないので対象ホストは Linuxでなくても良い）か、新しいグループを作成する。</p>

<p>下の方に「エージェントのインターフェース」欄があり、ここに Zabbixエージェントの接続情報を入力する必要がある。Web監視だけしか行わないのであればエージェントの設定は本来不要なはずだが、Zabbixの仕様上使わない場合でも入力しておく必要があるようなので <strong>DNS名</strong>にホスト名を入力して接続方法を「DNS」にだけしておく。</p>
<p><img src="/blog/img/create-host2.png" class="img-responsive img-thumbnail"></p>
<p>「ホスト」タブの入力を終えてもまだ下の「保存」ボタンはクリックせず「テンプレート」タブに移り、この監視対象ホストを <strong>"Template App HTTP Service"</strong> テンプレートに紐づける。<strong>新規テンプレートをリンク</strong>欄にテンプレート名の一部（例えば "HTTP"）を入力すると候補が表示されるのでそこから目的のテンプレートを選択する。</p>

<img src="/blog/img/create-host3.png" class="img-fluid img-thumbnail">&nbsp;

<p>テンプレートを選択したら「追加」をクリックするのを忘れないこと。これを忘れるとせっかく選択したテンプレートはホストに紐付けされない。</p>

<img src="/blog/img/create-host4.png" class="img-fluid img-thumbnail">&nbsp;

<p>テンプレートがホストに紐付けされたら、「保存」ボタンをクリックして監視対象ホストの登録を完了する。</p>

<img src="/blog/img/create-host5.png" class="img-fluid img-thumbnail">&nbsp;

<p>監視対象ホストの登録を完了すると、ホスト一覧が表示されここに新しく追加されていることが確認できる。</p>

<img src="/blog/img/create-host6.png" class="img-fluid img-thumbnail">&nbsp;

<h3>監視対象の状態を確認</h3>

<p>監視対象のホストが登録されるとすぐに Zabbixサーバーは監視を開始する。監視対象がどのような状態か確認するには、メニューから「監視データ」「最新データ」をたどってグループ・ホストを絞り込む。（監視対象が一つしか無いうちは絞り込むまでもないが）</p>

<img src="/blog/img/data1.png" class="img-fluid img-thumbnail">&nbsp;

<p>今回登録したホストには "Template App HTTP Service" テンプレートが適用されているため、HTTP Serviceなるアイテム（監視項目）が監視されている。"HTTP service is running" の「最新の値」が Up(1) になっていれば、監視対象のサーバーで Apacheなどの Webサーバーが起動しているということになる。<strong>試しに対象サーバの Apacheを停止してみる</strong>と値が Down(0)になり、障害を通知するメールが先刻設定したメールアドレス宛に飛ぶはずだ。</p>

<p>もし Zabbix上でのメール関連設定が正しいのにメールが届かない場合は迷惑メール扱いになっていないか確認した上で SMTPサーバのログをチェックして問題を解決する必要がある。また Walbrix版 Zabbixは SMTPサーバを兼ねているが、この構成だとOP25Bを実施しているインターネットプロバイダに接続している場合にはメールが飛ばないだろう。</p>

<p>OP25B対策についての参考記事: <a href="./2014-02-gentoo-postfix-relay-gmail.html">OP25Bを回避するため Postfixのリレー先を gmailに設定する方法</a></p>

<img src="/blog/img/data2.png" class="img-fluid img-thumbnail">&nbsp;

<p>障害が発生するとこのように Zabbixのトップページにもオレンジ色で警告が表示される。</p>

<img src="/blog/img/fail.png" class="img-fluid img-thumbnail">&nbsp;

<p>試しに落とした監視対象ホストの Apacheを起動しなおすと警告は表示されなくなり、復帰を知らせるアラートメールが送信される。</p>

<h3>Webシナリオの登録</h3>
<p>ここまでの設定だけでは、監視対象のWebサーバが HTTPのポート（80番）で待ち受けをしているかどうかの監視しか出来ない。具体的には、設定不備やアプリケーションサーバのクラッシュでページが 500 Internel Server Errorになっていたり、ファイルを謝って削除して 404 Not Foundになっていたり、アクセス集中でページの表示に何十秒もかかるような状態になっていたりしても Webサーバ自体が落ちていない限り障害として検知されない。</p>

<p>それらのような障害も検知するには、監視対象ホストに Webシナリオを登録する必要がある。</p>

<h4>Webシナリオとは</h4>
<p>Webシナリオとは、Zabbixサーバが監視対象のWebサーバを巡回する一連の手順をいう。Zabbixサーバはシナリオに記された URLに上から順にアクセスし、途中でひとつでも異常があればそれを障害として検知する。</p>

<img src="/blog/img/web-scenario.png" class="img-fluid img-thumbnail">&nbsp;

<p>本記事では例としてサイトのトップページにだけアクセスする最も簡単なシナリオを作る。</p>

<h4>Webシナリオの新規作成</h4>
<p>メニューから「設定」「ホスト」でホスト設定画面へ行き、対象のサーバを見つけ出して「ウェブ」をクリックする</p>

<img src="/blog/img/web-monitoring1.png" class="img-fluid img-thumbnail">&nbsp;

<p>「ウェブ監視の設定」画面になるので「シナリオの作成」をクリック</p>

<img src="/blog/img/web-monitoring2.png" class="img-fluid img-thumbnail">&nbsp;

<p>シナリオに<strong>名前</strong>を付け、<strong>アプリケーション</strong>には "HTTP service" を選択する（Zabbixでいうところの「アプリケーション」とは監視項目のグループといった意味で、ここではそれ以上の深い意味はない）。<strong>更新間隔</strong>はこのシナリオを実行して対象のWebサイトをチェックする間隔で、デフォルトは60秒となっているが、これは60秒おきに対象Webサーバのログにアクセス記録が残ることを意味するのでログファイルをあまり汚したくない場合などはもっと長い間隔を指定すると良い。</p>

<p>この時点ではまだ「保存」ボタンを押さないこと。</p>

<img src="/blog/img/web-monitoring3.png" class="img-fluid img-thumbnail">&nbsp;

<h4>Webシナリオにステップを追加</h4>
<p>「ステップ」タブを選択し、「追加」をクリックする</p>

<img src="/blog/img/web-monitoring4.png" class="img-fluid img-thumbnail">&nbsp;

<p>ステップに適当な<strong>名前</strong>をつけ、<strong>URL</strong>を入力する。</p>
<p>タイムアウト（デフォルトで15秒）は、その時間内にサーバーが応答を返さなければ障害扱いにする。</p>
<p><strong>要求文字列</strong>を指定すると、その URLにアクセスしたときにサーバーから返ってくる内容にその文字列が入っていることをチェックし、入っていない場合はそれも障害扱いとする。空欄にしておけば応答内容のチェックは行われない。</p>
<p><strong>要求ステータスコード</strong>には通常 200 を入れておけば良い。200 OK以外のHTTP応答コードを期待するような特殊なURLではそれ以外の値を入力することもできる。</p>
<p>ステップの項目を入力し終えたら「追加」ボタンをクリックする。</p>

<img src="/blog/img/web-monitoring5.png" class="img-fluid img-thumbnail">&nbsp;

<p>必要な全てのステップを追加し終えたら「保存」を押して Webシナリオの作成を完了し、一覧に新しいシナリオが追加されていることを確認する。</p>

<img src="/blog/img/web-monitoring6.png" class="img-fluid img-thumbnail">&nbsp;

<img src="/blog/img/web-monitoring7.png" class="img-fluid img-thumbnail">&nbsp;

<h4>Webシナリオの実行状況を確認</h4>
<p>メニューから「監視データ」「ウェブ」を開くと、ウェブシナリオの実行状況を確認することができる。シナリオ名をクリックするとサイトの応答時間がグラフで表示される。</p>

<img src="/blog/img/web-monitoring8.png" class="img-fluid img-thumbnail">&nbsp;

<img src="/blog/img/web-monitoring9.png" class="img-fluid img-thumbnail">&nbsp;

<p>「設定」「ホスト」画面から監視対象サーバーの「ウェブ」を選択することでシナリオを修正できるので、サイトのトップページ以外のURLもシナリオに追加してみて、わざとそのページのHTMLファイルを削除するなどしてシナリオの実行が正しく失敗扱いになるか確認すると良いだろう。</p>

<h3>Webシナリオにトリガーを設定する</h3>
<p>トリガーとは、ご存じのとおり日本語に直訳すると「引き金」である。Webシナリオを作っただけだと、シナリオの実行に失敗しても Zabbixサーバーはそれをまだ正式に「障害」とは認識しない（ので障害通知のアラートメールも飛ばない）。Webサイトに障害が発生した際にアラートメールが飛ぶようにするには、「Webシナリオの失敗→障害」という引き金を定義してやらなくてはならない。</p>

<p>トリガーを作成するにはメニューから「設定」「ホスト」で監視対象ホストを表示し、「トリガー」を開いて「トリガーの作成」をクリックする。</p>

<img src="/blog/img/trigger1.png" class="img-fluid img-thumbnail">&nbsp;

<img src="/blog/img/trigger2.png" class="img-fluid img-thumbnail">&nbsp;

<p>トリガーに適当な<strong>名前</strong>を付け、「追加」ボタンでトリガー発動の<strong>条件式</strong>を入力（後述）し、<strong>深刻度</strong>を選択して「保存」する</p>

<img src="/blog/img/trigger3.png" class="img-fluid img-thumbnail">&nbsp;

<p>条件式にはは式を直接入力することも出来るが、「追加」ボタンで開くウィンドウから選択して入力したほうが簡単だ。<strong>アイテム</strong>として "Failed step of scenario..." を、<strong>関数</strong>には「最新(T秒前/T個前)の値 NOT N」</p>を選択して「挿入」ボタンで条件式を挿入する。この条件式は「直近に行ったWebシナリオの実行が失敗した」という意味になる。</p>

<img src="/blog/img/trigger4.png" class="img-fluid img-thumbnail">&nbsp;

<p>これでWebシナリオにトリガーが設定され、障害検知（サイトの閲覧が正しく出来ない状態になった）時（と復旧時）にアラートメールが送られるようになったはずだ。</p>
