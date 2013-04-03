define [
  'hchart'
], ($)->
  options =
    chart:
      type: 'pie'
      renderTo: 'chartContainer'
    series: [
      {
      name: 'Total Translations'
      size: '60%'
      dataLabels:
        color: 'white'
        distance: -30
        formatter: ()->
          return @point.name if @y > 5
          null
      }
      {
      name: 'Distinct translations'
      size: '80%'
      innerSize: '60%'
      dataLabels:
        formatter: ()->
          return "<b>#{@point.name}: </b> #{@y}" if @y > 1
          return null
      }
    ]
    title:
      text: ''
    subtitle:
      text: ''
    tooltip:
      valueSuffix: '%'
    plotOptions:
      pie:
        shadow: false
        center: ['50%', '50%']

  reportChart = null

  showChart=(title, json, chart = Highcharts.charts[0])->
    return if !json
    console?.log json

    options.title.text = title
    options.subtitle.text = "Translation number: #{json.translationNum}"

    options.series[0].data = [
      ['Distinct translations', json.distinctTranslationNum]
      ['Duplicated translations', json.translationNum - json.distinctTranslationNum]
    ]

    options.series[1].data = [
      ['Translated', json.translatedNum]
      ['Untranslated', json.untranslatedNum]
    ]
    reportChart = new Highcharts.Chart options

  showChart: showChart
  reportChart: reportChart




