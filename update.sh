#!/bin/bash
echo "Actualizando Tasker..."
cd ~/.tk

java -jar ~/.tk/tk.jar "$@"

echo "Compilando..."
javac --release 25 -d bin src/main/java/Main.java src/main/java/*.java

if [ $? -eq 0 ]; then
  jar cfm tk.jar MANIFEST.MF -C bin .
  echo "Tasker actualizado correctamente!"
else
  echo "Error en compilación"
fi
