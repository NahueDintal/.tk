#!/bin/bash
echo "Actualizando Tasker..."
cd ~/.tk

echo "Compilando..."
javac --release 25 -d bin src/main/java/Main.java src/main/java/*.java

if [ $? -eq 0 ]; then
  # Crear MANIFEST.MF si no existe
  echo "Main-Class: Main" > MANIFEST.MF
  echo "Class-Path: ." >> MANIFEST.MF
  
  jar cfm tk.jar MANIFEST.MF -C bin .
  echo "Tasker actualizado correctamente!"
  
  # Opcional: probar la ejecución
  echo "Probando ejecución..."
  java -jar tk.jar --help
else
  echo "Error en compilación"
  exit 1
fi
