pkgname='immogram'
pkgver=1.0.0
pkgrel=1
pkgdesc='A telegram bot that scrapes the web'
arch=('any')
depends=('java-environment>=13' 'groovy')
makedepends=('maven')

license=('custom')
url='https://github.com/ILadis/immogram'

build() {
  cd ..
  mvn clean package
}

package() {
  cd ..
  unzip -d target/bin target/*-bin.zip
  cd target/bin/immogram-*

  mkdir -p \
    "${pkgdir}/usr/lib/immogram/lib/" \
    "${pkgdir}/usr/lib/systemd/system/" \
    "${pkgdir}/usr/lib/sysusers.d/" \
    "${pkgdir}/usr/lib/tmpfiles.d/"

  install -Dm 644 main.groovy \
    "${pkgdir}/usr/lib/immogram/main.groovy"

  install -Dm 644 lib/* \
    "${pkgdir}/usr/lib/immogram/lib/"

  echo 'u immogram - - / /usr/bin/nologin' \
    > "${pkgdir}/usr/lib/sysusers.d/immogram.conf"

  echo 'd /var/lib/immogram 0750 immogram immogram -' \
    > "${pkgdir}/usr/lib/tmpfiles.d/immogram.conf"

  cat << EOF > "${pkgdir}/usr/lib/systemd/system/immogram.service"
[Unit]
Description=immogram
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=immogram
Group=immogram
Environment="LIBS=/usr/lib/immogram/lib/"
Environment="PROPS=/var/lib/immogram/props"
WorkingDirectory=/var/lib/immogram/
ExecStart=groovy /usr/lib/immogram/main.groovy
Restart=on-abort

[Install]
WantedBy=multi-user.target
EOF
}
