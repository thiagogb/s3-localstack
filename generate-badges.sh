#!/bin/bash

# Diretório onde as badges serão salvas
BADGES_DIR=".github/badges"

# Certifique-se de que o diretório existe
mkdir -p $BADGES_DIR

# Arquivo CSV do JaCoCo
JACOCO_CSV="target/site/jacoco/jacoco.csv"

# Verifica se o arquivo CSV existe
if [ ! -f "$JACOCO_CSV" ]; then
    echo "Arquivo $JACOCO_CSV não encontrado. Execute 'mvn test jacoco:report' primeiro."
    exit 1
fi

# Extrai a cobertura de linha total
LINE_COVERAGE=$(tail -n +2 $JACOCO_CSV | awk -F, '{covered+=$7; missed+=$6} END {print covered/(covered+missed)*100}')
LINE_COVERAGE_ROUNDED=$(printf "%.1f" $LINE_COVERAGE)

# Extrai a cobertura de branch total
BRANCH_COVERAGE=$(tail -n +2 $JACOCO_CSV | awk -F, '{covered+=$5; missed+=$4} END {print covered/(covered+missed)*100}')
BRANCH_COVERAGE_ROUNDED=$(printf "%.1f" $BRANCH_COVERAGE)

# Determina a cor com base na cobertura
function get_color() {
    local coverage=$1
    if (( $(echo "$coverage < 50" | bc -l) )); then
        echo "e05d44" # vermelho
    elif (( $(echo "$coverage < 80" | bc -l) )); then
        echo "dfb317" # amarelo
    else
        echo "4c1" # verde
    fi
}

LINE_COLOR=$(get_color $LINE_COVERAGE)
BRANCH_COLOR=$(get_color $BRANCH_COVERAGE)

# Cria a badge de cobertura de linha
cat > $BADGES_DIR/jacoco.svg << EOF
<svg xmlns="http://www.w3.org/2000/svg" width="106" height="20" role="img" aria-label="coverage: ${LINE_COVERAGE_ROUNDED}%">
  <title>coverage: ${LINE_COVERAGE_ROUNDED}%</title>
  <linearGradient id="s" x2="0" y2="100%">
    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <clipPath id="r">
    <rect width="106" height="20" rx="3" fill="#fff"/>
  </clipPath>
  <g clip-path="url(#r)">
    <rect width="61" height="20" fill="#555"/>
    <rect x="61" width="45" height="20" fill="#${LINE_COLOR}"/>
    <rect width="106" height="20" fill="url(#s)"/>
  </g>
  <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" text-rendering="geometricPrecision" font-size="110">
    <text aria-hidden="true" x="315" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="510">coverage</text>
    <text x="315" y="140" transform="scale(.1)" fill="#fff" textLength="510">coverage</text>
    <text aria-hidden="true" x="825" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="350">${LINE_COVERAGE_ROUNDED}%</text>
    <text x="825" y="140" transform="scale(.1)" fill="#fff" textLength="350">${LINE_COVERAGE_ROUNDED}%</text>
  </g>
</svg>
EOF

# Cria a badge de cobertura de branch
cat > $BADGES_DIR/branches.svg << EOF
<svg xmlns="http://www.w3.org/2000/svg" width="106" height="20" role="img" aria-label="branches: ${BRANCH_COVERAGE_ROUNDED}%">
  <title>branches: ${BRANCH_COVERAGE_ROUNDED}%</title>
  <linearGradient id="s" x2="0" y2="100%">
    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <clipPath id="r">
    <rect width="106" height="20" rx="3" fill="#fff"/>
  </clipPath>
  <g clip-path="url(#r)">
    <rect width="61" height="20" fill="#555"/>
    <rect x="61" width="45" height="20" fill="#${BRANCH_COLOR}"/>
    <rect width="106" height="20" fill="url(#s)"/>
  </g>
  <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" text-rendering="geometricPrecision" font-size="110">
    <text aria-hidden="true" x="315" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="510">branches</text>
    <text x="315" y="140" transform="scale(.1)" fill="#fff" textLength="510">branches</text>
    <text aria-hidden="true" x="825" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="350">${BRANCH_COVERAGE_ROUNDED}%</text>
    <text x="825" y="140" transform="scale(.1)" fill="#fff" textLength="350">${BRANCH_COVERAGE_ROUNDED}%</text>
  </g>
</svg>
EOF

echo "Badges geradas com sucesso em $BADGES_DIR:"
echo "- Cobertura de linha: ${LINE_COVERAGE_ROUNDED}%"
echo "- Cobertura de branch: ${BRANCH_COVERAGE_ROUNDED}%" 