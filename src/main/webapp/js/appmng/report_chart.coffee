define [ 'hchart'], ($)->
  options =
    chart:
      type: 'pie'
#      plotBackgroundColor: 'pink'
      plotBorderWidth: null
      plotShadow: false
    tooltip:
#      enabled: false
      shared: true
      formatter: ()->"<span style='color:#{@series.color}'>#{@point.name}</span>: <br/><b>#{@point.y}(#{@.point.percentage.toFixed(2)}%)</b><br/>"
    title:
      text: ''
    legend:
      align: 'right'
      layout: 'vertical'
      verticalAlign: 'middle'
      width: 100
      style: 'font-size: 8px'

    plotOptions:
      pie:
        showInLegend: true
        allowPointSelect: true
        cursor: 'pointer'
        size: '80%'
        dataLabels:
          enabled: false
    series: [
      name: 'Total translations'
    ]

  colors = Highcharts.getOptions().colors


  showChart=(title, json)->
    return if !json
#    console?.log json
#    options.title.text = title
#    options.subtitle.text = "Translation number: #{json.translationNum}"

    options.series[0].data = [
      {name: 'Duplicated', y: json.translationNum - json.distinctTranslationNum}
      {name: 'Distinct', y: json.distinctTranslationNum, color: colors[3]}
    ]
    $('#dupContainer').highcharts(options)

    options.series[0].name = 'Distinct Translations'
    options.series[0].data = [
      ['Translated', json.translatedNum]
      {name: 'Untranslated', y:json.untranslatedNum, color: colors[3]}
    ]
    $('#transContainer').highcharts(options)

    options.series[0].data = [
      ['Auto trans', json.matchedNum]
      {name: 'No match', y: json.distinctTranslationNum - json.matchedNum, color: colors[3]}
    ]
    $('#autoTransContainer').highcharts(options)

    # remove Highchars logo
    $("tspan:contains('Highcharts.com')").remove()

  showChart: showChart




