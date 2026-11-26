#!/bin/sh

set -e

for file in *.yaml; do
    consul kv put "config/${file%.yaml}/data" "@$file"
done
