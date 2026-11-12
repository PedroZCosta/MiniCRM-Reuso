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
    public class HistoricoEtapaController : Controller
    {
        private readonly MiniCrmContext _context;

        public HistoricoEtapaController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: HistoricoEtapa
        public async Task<IActionResult> Index()
        {
            var miniCrmContext = _context.HistoricoEtapa.Include(h => h.IdOportunidadeNavigation).Include(h => h.IdUsuarioNavigation);
            return View(await miniCrmContext.ToListAsync());
        }

        // GET: HistoricoEtapa/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var historicoEtapa = await _context.HistoricoEtapa
                .Include(h => h.IdOportunidadeNavigation)
                .Include(h => h.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdHistorico == id);
            if (historicoEtapa == null)
            {
                return NotFound();
            }

            return View(historicoEtapa);
        }

        // GET: HistoricoEtapa/Create
        public IActionResult Create()
        {
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidade, "IdOportunidade", "Etapa");
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email");
            return View();
        }

        // POST: HistoricoEtapa/Create
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
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidade, "IdOportunidade", "Etapa", historicoEtapa.IdOportunidade);
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", historicoEtapa.IdUsuario);
            return View(historicoEtapa);
        }

        // GET: HistoricoEtapa/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var historicoEtapa = await _context.HistoricoEtapa.FindAsync(id);
            if (historicoEtapa == null)
            {
                return NotFound();
            }
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidade, "IdOportunidade", "Etapa", historicoEtapa.IdOportunidade);
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", historicoEtapa.IdUsuario);
            return View(historicoEtapa);
        }

        // POST: HistoricoEtapa/Edit/5
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
            ViewData["IdOportunidade"] = new SelectList(_context.Oportunidade, "IdOportunidade", "Etapa", historicoEtapa.IdOportunidade);
            ViewData["IdUsuario"] = new SelectList(_context.Usuario, "IdUsuario", "Email", historicoEtapa.IdUsuario);
            return View(historicoEtapa);
        }

        // GET: HistoricoEtapa/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var historicoEtapa = await _context.HistoricoEtapa
                .Include(h => h.IdOportunidadeNavigation)
                .Include(h => h.IdUsuarioNavigation)
                .FirstOrDefaultAsync(m => m.IdHistorico == id);
            if (historicoEtapa == null)
            {
                return NotFound();
            }

            return View(historicoEtapa);
        }

        // POST: HistoricoEtapa/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var historicoEtapa = await _context.HistoricoEtapa.FindAsync(id);
            if (historicoEtapa != null)
            {
                _context.HistoricoEtapa.Remove(historicoEtapa);
            }

            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool HistoricoEtapaExists(int id)
        {
            return _context.HistoricoEtapa.Any(e => e.IdHistorico == id);
        }
    }
}
