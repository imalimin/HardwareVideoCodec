#!/bin/bash


echo ------- Addr2Line --------

LIB_DIR=$1/build/intermediates/cmake/debug/obj/armeabi-v7a
echo "addr:$2, lib:${LIB_DIR}"
echo ""

addr2line(){

	RESULT=` arm-linux-androideabi-addr2line -e "${LIB_DIR}/$1" $2 `
	if [[ ! $RESULT =~ "?" ]]
	then
		echo "found ${1}: ${RESULT##*/}"
	fi

}

list(){
	for file in ` ls $1 `
	do
		addr2line $file $2
	done
}

list ${LIB_DIR} $2
