#!/bin/bash
# Corre DNSGA-II A y B sobre el trafico de Traffic/ (una ronda por proceso, varias en paralelo)
# y despues la evaluacion de los metodos (MCDMExperiment).
# Uso: ./correr.sh [generaciones_por_ventana] [rondas] [procesos_en_paralelo]
GEN=${1:-50}
ROUNDS=${2:-30}
PARALLEL=${3:-12}

HOME_DIR="$(cd "$(dirname "$0")" && (pwd -W 2>/dev/null || pwd))"
PROJECT="$HOME_DIR/../../Code/DMOP"
SEP=":"
[ "$OS" = "Windows_NT" ] && SEP=";"

# Si existe el repositorio maven local del proyecto se usa sin internet
MVN="mvn -q"
[ -d "$HOME_DIR/../../run-local/maven-repository" ] && MVN="mvn -q -o -Dmaven.repo.local=$HOME_DIR/../../run-local/maven-repository"

# Se compila y se copian las clases, asi se puede recompilar mientras corre
(cd "$PROJECT" && $MVN compile) || exit 1
rm -rf "$HOME_DIR/classes" && cp -r "$PROJECT/target/classes" "$HOME_DIR/classes"
DEPS=$(cd "$PROJECT" && $MVN org.codehaus.mojo:exec-maven-plugin:3.0.0:exec -Dexec.executable=echo -Dexec.args=%classpath | tail -1)
CP="$HOME_DIR/classes$SEP$DEPS"
export HOME_DIR CP GEN
mkdir -p "$HOME_DIR/logs" "$HOME_DIR/Solutions"

echo "$(date '+%F %T') inicio: GEN=$GEN ROUNDS=$ROUNDS PARALLEL=$PARALLEL" >> "$HOME_DIR/logs/estado.txt"

for ALG in DNSGAII-A DNSGAII-B; do
  for R in $(seq 1 $ROUNDS); do echo "$ALG $R"; done
done | xargs -P "$PARALLEL" -L 1 bash -c '
  java -Xmx1g -Dapp.home="$HOME_DIR/" -Dlog4j.configuration="file:///$HOME_DIR/log4j-quiet.properties" \
    -cp "$CP" py.edu.fiuni.dmop.DynamicSolverRunner "$0" "$1" "$GEN" > "$HOME_DIR/logs/$0_r$1.log" 2>&1
  echo "$(date "+%F %T") termino $0 r$1 (codigo $?)" >> "$HOME_DIR/logs/estado.txt"
'

echo "$(date '+%F %T') DNSGA-II terminado: $(ls $HOME_DIR/Solutions/*.dat 2>/dev/null | wc -l) frentes" >> "$HOME_DIR/logs/estado.txt"

java -Xmx2g -Dapp.home="$HOME_DIR/" -Dstdout.encoding=UTF-8 -Dlog4j.configuration="file:///$HOME_DIR/log4j-quiet.properties" \
  -cp "$CP" py.edu.fiuni.dmop.MCDMExperiment DNSGAII-A,DNSGAII-B "$ROUNDS" 10 > "$HOME_DIR/logs/experimento.log" 2>&1

echo "$(date '+%F %T') experimento terminado (codigo $?)" >> "$HOME_DIR/logs/estado.txt"
