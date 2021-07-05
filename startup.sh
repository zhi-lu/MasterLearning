#!/usr/bin/env zsh
# shellcheck disable=SC2164
# shellcheck disable=SC2035
# shellcheck disable=SC2103
# 详细参数解释请看站长.
hello="你好吖,[]~（￣▽￣）~*.正在开启tomcat,<（￣︶￣）>."
echo "${hello}"
rm -rf bootstrap.jar
jar cvf0 bootstrap.jar -C out/production/MasterLearning com/luzhi/miniTomcat/Bootstrap.class -C out/production/MasterLearning com/luzhi/miniTomcat/classloader/CommonClassLoader.class
# 删除 lib 目录下的miniTomcat.jar
rm -rf lib/miniTomcat.jar
cd out
cd production
cd MasterLearning
jar cvf0 ../../../lib/miniTomcat.jar *
cd ..
cd ..
cd ..
java -cp bootstrap.jar com/luzhi/miniTomcat/Bootstrap