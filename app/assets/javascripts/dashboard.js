(() => {
    window.addEventListener('load', (event) => {
      const statusGroups = document.getElementById('statusGroups')
      const statusGroupWithDocuments = statusGroups.dataset.statusGroup
      if (statusGroupWithDocuments != "submitted") {
          const container = document.querySelector('[data-module="govuk-tabs"]')
          const tabs = new window.GOVUKFrontend.Tabs(container)
          const currentTab = tabs.getCurrentTab()
          tabs.hideTab(currentTab)

          const newTab = document.querySelector(`a#tab_${statusGroupWithDocuments}-submissions.govuk-tabs__tab`)
          tabs.showTab(newTab)
          tabs.createHistoryEntry(newTab)
      }

      const baseHref = statusGroups.dataset.href
      const statuses = ['submitted', 'action', 'rejected', 'cancelled']
      Array.from(document.querySelectorAll('a.govuk-tabs__tab'), (link, ix) => {
          link.removeEventListener('click', link.boundTabClick, true)
          link.removeEventListener('keydown', link.boundTabKeydown, true)
          link.href = `${baseHref}${statuses[ix]}&page=1`
      })
    })
})();