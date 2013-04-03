$(($)->
  colors = Highcharts.getOptions().colors

  msg = """
        {
        "dictNum": 5,
        "labelNum": 247,
        "translationNum": 5435,
        "translationWC": 34141,

        "distinctTranslationNum": 4503,
        "distinctTranslationWC": 30813,

        "untranslatedNum": 299,
        "untranslatedWC": 1301,
        "translatedNum": 4204,
        "translatedWC": 29512,

        "matchedNum": 391,
        "matchedWC": 2656
        }
        """
  json = $.parseJSON(msg)

  data = [
    { name: 'Distinct', y: json.distinctTranslationNum, drilldown: [
      {name: 'Translated', y: json.untranslatedNum}
      {name: 'Untranslated', y: json.translatedNum}
    ]}
    { name: 'Duplicated', y: json.translationNum - json.distinctTranslationNum, drilldown: [
      {name: 'Duplicated', y: json.translationNum - json.distinctTranslationNum}
    ]}
  ]

  browserData = []
  versionsData = []
  for entry, idx in data
    browserData.push name: "#{entry.name} translations", y: entry.y, color: colors[idx]
    for subEntry, subIdx in entry.drilldown
      color = Highcharts.Color(colors[idx]).brighten(0.2 - subIdx / entry.drilldown.length / 10).get()
      versionsData.push name: "#{subEntry.name} translations", y: subEntry.y, color: color

  $('#container').highcharts(
    chart:
      type: 'pie'
#      backgroundColor: 'grey'
    title:
      text: 'Browser market share, April, 2011'
    yAxis:
      title:
        text: 'Total percent market share'
    plotOptions:
      pie:
        shadow: false,
        center: ['50%', '50%']
#    tooltip:

    series: [
      {
      name: 'Total translations',
      data: browserData,
      size: '60%',
      dataLabels:
        formatter: ()->"#{@point.name}: #{@point.y}(#{@point.percentage.toFixed(2)}%)"
        color: 'white',
        distance: -30
      }
      {
      name: 'Distict translations',
      data: versionsData,
      size: '80%',
      innerSize: '60%',
      dataLabels:
        formatter: ()->"#{@point.name}: #{@point.y}(#{@point.percentage.toFixed(2)}%)"
      }
    ]
  )
)


