if [ -z "$2" ]
then
    echo "Specify language and name for it i.e. en English";
    exit -1;
fi

pojson i18n/${1}/LC_MESSAGES/messages.po > i18n/${1}.json
python3 i18n/correctJSON.py ${1} ${2}