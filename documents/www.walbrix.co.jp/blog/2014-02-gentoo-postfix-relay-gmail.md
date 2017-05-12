---
category: ブログ
title: OP25Bを回避するため Postfixのリレー先を gmailに設定する方法
summary: プライベートネットワーク内に設置されたSMTPサーバが外へメールをリレーする際には通常25番のポートを使用するが、利用しているISPによってはOP25B方針によって遮断されてしまうため 587番ポートでのサブミッションを受け付ける gmailのSMTPサーバにリレーしてもらうよう設定してみる。
date: 2014-02-12
page_image: /blog/img/email.jpg
template: www.walbrix.co.jp/page.html.j2
jumbotron_class: simple
adsense: true
---

### OP25Bとは

<p>Outbound Port 25 Blocking (<a href="http://ja.wikipedia.org/wiki/Outbound_Port_25_Blocking">Wikipediaによる解説</a>)の略。</p>
<p>迷惑メールを大量送信する目的でサービスに加入する利用者への対策として近年ISPで取られている方策。インターネット側への25番ポート(SMTP)による直接アクセスをブロックする。</p>
<h4>普通のインターネットユーザーがメールを送るとき</h4>
<p>インターネットサービスプロバイダ(ISP)と契約すると、大抵はメールアドレスが発行され、そのアドレスでメールを送受信するための SMTPサーバとPOP3サーバが通知される。一般的なインターネットユーザーはこの時に通知されたSMTPサーバを用いて外部にメールを送信する。</p>

<img class="img-thumbnail img-fluid" src="/blog/img/op25b01.png">&nbsp;

<h4>迷惑メール送信業者の手口</h4>
<p>迷惑メールの送信業者は ISPの提供するSMTPサーバを用いず、相手側メールサーバの 25番ポートへ直接通信を行うことでメールを高速かつ大量に配信する。</p>

<img class="img-thumbnail img-responsive" src="/blog/img/op25b02.png">&nbsp;

<h4>プロバイダ側の対抗策 OP25Bの登場</h4>
<p>光ファイバを用いた高速回線が月額5千円程度で利用出来る日本は特にそうなのだが、個人向けインターネット接続サービスは利用料が安い上に規約違反で退会処分とされても別名義による再契約が容易なため迷惑メール業者にとってきわめて都合の良い環境となってしまった。そこで各 ISPは対抗措置として OP25Bを開始、25番ポートへの直接アクセスはブロックされることとなる。</p>

<img class="img-thumbnail img-fluid" src="/blog/img/op25b03.png">&nbsp;

#### とばっちりを食ってしまった人(このページの読者)

<p>ところが、迷惑メール業者ではない善良な利用者の中にもメールを直接25番ポートで相手先のメールサーバへ送信したい人は存在する。プロバイダが提供しているもの以外のメールアドレスを使って送受信をする人や、興味または業務上の理由で自前のメールサーバを持って運用している人々がそうだ。自分のサーバで PostfixなどのSMTPサーバを利用してメールを処理させた場合、通常の設定だと相手側メールサーバの25番ポートへ直接接続しにいってしまう。</p>

<img class="img-thumbnail img-fluid" src="/blog/img/op25b04.png">&nbsp;

#### gmailのサーバーを使う解決方法

自分のメールサーバを運用している場合は、相手先のメールサーバへ直接メールを配信する代わりに gmailのSMTPサーバへ一旦引き渡すように設定することでこの問題を回避できる。但し gmailのSMTPサーバを利用するには gmailのアカウントが必要となるし、迷惑メールの送信などを行えばアカウントを削除されかねないのであくまで善良な目的にのみ使用できる。

<img class="img-thumbnail img-fluid" src="/blog/img/op25b05.png">&nbsp;

### Postfixの設定

ようやく本題に入り、GmailのSMTPサーバをリレー先として使用するように Postfixを設定する。手順は Gentoo Linuxを例としている。

#### sasl USEフラグ付きの Postfixを emergeする

gmailのSMTPサーバは認証を要求するのだが、PostfixがSMTP認証を行うには saslが有効になっている必要がある。有効になっていない場合、<strong>/etc/portage/package.use</strong> ファイルに下記のような行を追加して Postfixに sasl USEフラグを追加し、postfixを emergeする。

```
mail-mta/postfix sasl
```

```
# emerge postfix
```

#### /etc/postfix/main.cf

Postfixのメイン設定ファイルにリレー先の設定を追記する。gmailのSMTPサーバは smtp.gmail.com で、ポート番号は 587を使うことができる。

```
relayhost = [smtp.gmail.com]:587
smtp_use_tls = yes
smtp_sasl_auth_enable = yes
smtp_sasl_password_maps = hash:/etc/postfix/saslpass
smtp_sasl_tls_security_options = noanonymous
smtp_sasl_mechanism_filter = plain
smtp_tls_CApath = /etc/ssl/certs/ca-certificates.crt
```

#### /etc/postfix/saslpass

パスワードはメイン設定ファイルとは別のファイル（smtp_sasl_password_maps行で指定されているもの）に<strong>[ホスト名]:ポート ID:パスワード</strong>の書式で記入する。

<pre>
[smtp.gmail.com]:587 example@gmail.com:<i>パスワード</i>
</pre>

<h4>/etc/postfix/saslpass.dbの作成</h4>
<p>パスワードを記入したファイルを、Postfixが読み込みやすい形式に postmap コマンドで変換する。変換後のファイル名には .db が付くが、設定ファイルで指定するファイル名は変換前のファイル名で良い。</p>

```
# postmap /etc/postfix/saslpass
```

#### Postfixを再起動

```
# /etc/init.d/postfix restart
```

#### telnetを使用した送信実験

telnetコマンド（Gentooの場合は emerge netkit-telnetdでインストール）を使って SMTPプロトコルを直接話すことで Postfixと対話し、メールがきちんと送信されるか確認する。太字が手による入力。

<pre>$ <strong>telnet localhost smtp</strong>
Connected to localhost.
Escape character is '^]'.
220 myhost.localdomain ESMTP Postfix
<strong>HELO example.com</strong>
250 myhost.localdomain
<strong>MAIL FROM: your_gmail_id@gmail.com</strong>
250 2.1.0 Ok
<strong>RCPT TO: awesomeshimarin@walbrix.com</strong>
250 2.1.5 Ok
<strong>DATA</strong>
354 End data with <CR><LF>.<CR><LF>
<strong>Subject: Hello!
This is a test mail.
.</strong>
250 2.0.0 Ok: queued as A032965B80E
</pre>

上記くらいのテストがソラで出来ると、水回りのトラブルに駆けつけて直してあげるとお金をもらえる水道屋さんのように、メールサーバのトラブルに駆けつけて直してあげることでお金をもらう便利な人になれる。

参考記事: [OBP25でもあきらめない。自宅サーバーでPostfixでスパム判定されないメールを送る方法 - それマグで！](http://takuya-1st.hatenablog.jp/entry/20121105/1352111436)
