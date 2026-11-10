using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers
{
    public class HistoricoEtapasController : Controller
    {
        private readonly MiniCrmContext _context;

        public HistoricoEtapasController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: HistoricoEtapas
        public async Task<IActionResult> Index()
        {
            var miniCrmContext = _context.HistoricoEtapas.Include(h => h.IdOportunidadeNavigation).Include(h => h.IdUsuarioNavigation);
            return View(await miniCrmContext.ToListAsync());
        }

        // GET: HistoricoEtapas/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var historicoEtapa = await _context.HistoricoEtapas
                .Include(h => h.IdOportunidadeNavigation)
                .Include(h => h.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdHistorico == id);
            if (historicoEtapa == null)
            {
                return NotFound();
            }

            return View(historicoEtapa);
        }

        // GET: HistoricoEtapas/Create
        public IActionResult Create()
        {
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidades, "IdOportunidade", "Etapa");
            ViewData["IdUsuario"] = new SelectList(_context.Usuarios, "IdUsuario", "Email");
            return View();
        }

        // POST: HistoricoEtapas/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("IdHistorico,AlteradoEm,EtapaAnterior,EtapaNova,IdOportunidade,IdUsuario")] HistoricoEtapa historicoEtapa)
        {
            if (ModelState.IsValid)
            {
                _context.Add(historicoEtapa);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidades, "IdOportunidade", "Etapa", historicoEtapa.IdOportunidade);
            ViewData["IdUsuario"] = new SelectList(_context.Usuarios, "IdUsuario", "Email", historicoEtapa.IdUsuario);
            return View(historicoEtapa);
        }

        // GET: HistoricoEtapas/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var historicoEtapa = await _context.HistoricoEtapas.FindAsync(id);
            if (historicoEtapa == null)
            {
                return NotFound();
            }
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidades, "IdOportunidade", "Etapa", historicoEtapa.IdOportunidade);
            ViewData["IdUsuario"] = new SelectList(_context.Usuarios, "IdUsuario", "Email", historicoEtapa.IdUsuario);
            return View(historicoEtapa);
        }

        // POST: HistoricoEtapas/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("IdHistorico,AlteradoEm,EtapaAnterior,EtapaNova,IdOportunidade,IdUsuario")] HistoricoEtapa historicoEtapa)
        {
            if (id != historicoEtapa.IdHistorico)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(historicoEtapa);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!HistoricoEtapaExists(historicoEtapa.IdHistorico))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidades, "IdOportunidade", "Etapa", historicoEtapa.IdOportunidade);
            ViewData["IdUsuario"] = new SelectList(_context.Usuarios, "IdUsuario", "Email", historicoEtapa.IdUsuario);
            return View(historicoEtapa);
        }

        // GET: HistoricoEtapas/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var historicoEtapa = await _context.HistoricoEtapas
                .Include(h => h.IdOportunidadeNavigation)
                .Include(h => h.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdHistorico == id);
            if (historicoEtapa == null)
            {
                return NotFound();
            }

            return View(historicoEtapa);
        }

        // POST: HistoricoEtapas/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var historicoEtapa = await _context.HistoricoEtapas.FindAsync(id);
            if (historicoEtapa != null)
            {
                _context.HistoricoEtapas.Remove(historicoEtapa);
            }

            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool HistoricoEtapaExists(int id)
        {
            return _context.HistoricoEtapas.Any(e => e.IdHistorico == id);
        }
    }
}
