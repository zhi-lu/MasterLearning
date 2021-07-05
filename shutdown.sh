#!/usr/bin/env zsh
# 结束由 startup.sh 启动的tomcat进程
# shellcheck disable=SC2006
# shellcheck disable=SC2009
echo "正在关闭 tomcat (╮（╯▽╰）╭ )"
# 查找启动进程PID号
Tomcat_Id=$(ps -ef | grep java | grep -v "grep" | awk '{print $2}')
# 判断$Tomcat_Id不为空
if [ "$Tomcat_Id" ]; then
  for id in $Tomcat_Id; do
    # 结束进程
    kill -9 "$id"
    echo "有关Tomcat_Id进程${id}已经结束,谢谢."
  done
  echo "tomcat 关闭完成（＞﹏＜）"

else
  echo "tomcat 进程不存在.请稍后尝试.(（￣３￣）a)"
fi
